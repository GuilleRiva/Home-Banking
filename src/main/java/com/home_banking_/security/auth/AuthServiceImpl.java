package com.home_banking_.security.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home_banking_.dto.auth.*;
import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.enums.IdempotencyOperation;
import com.home_banking_.enums.Rol;
import com.home_banking_.enums.JwtTokenType;
import com.home_banking_.enums.UserStatus;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.model.IdempotencyRecord;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.idempotency.IdempotencyService;
import com.home_banking_.service.idempotency.IdempotencyValidationResult;
import com.home_banking_.service.impl.JwtService;
import com.home_banking_.security.token.Token;
import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.user.UserDetailsImpl;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.GeoLocationService;
import com.home_banking_.service.IPAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UsersRepository usersRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final IPAddressService ipAddressService;
    private final GeoLocationService geoLocationService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;


    @Value("${application.security.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    private record AuthTokens(String accessToken, String refreshToken) {}

    @Override
    @Transactional
    public RegisterResponseDto register(String idempotencyKey,RegisterRequestDto request) {
        Long userId = null;

        IdempotencyValidationResult validationResult =
                idempotencyService.validateAndRegister(
                        idempotencyKey,
                        null,
                        IdempotencyOperation.REGISTER,
                        request
                );

        if (validationResult.isReplay()){
            log.info("[REGISTER_IDEMPOTENT_REPLAY] idempotencyKey={}", idempotencyKey);
            return replayResponse(validationResult.getRecord());
        }

        RegisterResponseDto responseDto = executeRegister(request);
        IdempotencyRecord record = validationResult.getRecord();

        try{
            String responseBody = objectMapper.writeValueAsString(responseDto);

            idempotencyService.markAsCompleted(
                    record.getId(),
                    201,
                    responseBody,
                    null
            );
            return responseDto;

        }catch (Exception ex) {
            String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Unexpected error during register";

            idempotencyService.markAsFailed(record.getId(), errorMessage);

            throw new BusinessException("Couldn't complete idempotent register operation");
        }
    }

    private RegisterResponseDto executeRegister(RegisterRequestDto request){
        String normalizedEmail = normalizeEmail(request.getEmail());

        validateEmailIsAvailable(normalizedEmail);

        Users users = buildPendingClientUser(request,normalizedEmail);

        Users savedUser = usersRepository.save(users);

        registerSuccessfulRegistrationAudit(savedUser);

        log.info("[REGISTER_SUCCESS] userId={} status={}",
                savedUser.getId(), savedUser.getUserStatus());

        return buildRegisterResponse(savedUser);
    }


    @Transactional
    public void savedUserToken(Users user, String jwt, JwtTokenType jwtTokenType) {
        Token token = Token.builder()
                .user(user)
                .token(jwt)
                .jwtTokenType(jwtTokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }


    @Transactional
    @Override
    public AuthResponse login(AuthRequest request, String ipAddress) {

        String normalizedEmail = normalizeEmail(request.getEmail());
        String normalizedIp = validateAndNormalizeIp(ipAddress);

        Users user = getUserByEmailOrThrow(normalizedEmail);

        validateAccountLockStatus(user,normalizedIp);
        validateUserIsActive(user,normalizedIp);

        Authentication auth = authenticateUser(normalizedEmail,request.getPassword(), user, normalizedIp);

        validateIpIsNotSuspicious(user,normalizedIp);

        resetFailedLoginAttempts(user);
        registerLoginIp(user,normalizedIp);

        UserDetails userDetails = (UserDetails) auth.getPrincipal();

        AuthTokens tokens = generateAndPersistTokens(user, userDetails);

        registerSuccessfulLogin(user,normalizedIp);

        return new AuthResponse(
                tokens.accessToken(),
                tokens.refreshToken(),
                "Bearer",
                accessExpirationMs / 1000
        );
    }



    @Transactional
    @Override
    public AuthResponse refreshToken(String refreshToken) {

        if (!jwtService.isTokenValid(refreshToken)) {
            log.warn("Refresh token validation failed due to invalid JWT");
            throw new BusinessException("Authentication failed");
        }

        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> {
                    log.warn("Refresh token validation failed because token was not found in persistence");
                    return new BusinessException("Authentication failed");
                });

        if (storedToken.getJwtTokenType() != JwtTokenType.REFRESH) {
            log.warn("Refresh token validation failed du to invalid token type");
            throw new BusinessException("Authentication failed");
        }

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            log.warn("Refresh token rejected because it is expired or revoked");
            throw new BusinessException("Authentication failed");
        }

        String email = jwtService.extractUsername(refreshToken);

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException("Authentication failed"));

        if (users.getUserStatus() != UserStatus.ACTIVE) {
            log.warn("Refresh token denied due to inactive user status. userId={}, status={}",
                    users.getId(), users.getUserStatus());
            auditLogService.registerEvent(
                    users.getId(),
                    "Refresh token denied due to user status: " + users.getUserStatus(),
                    "REFRESH_FAILED",
                    "AUTH"
            );
            throw new BusinessException("Authentication failed");
        }

        if (users.isAccountLocked()) {
            log.warn("Refresh token denied due because is locked. userId={}",
                    users.getId());
            auditLogService.registerEvent(
                    users.getId(),
                    "Refresh token denied because account is locked",
                    "REFRESH_FAILED",
                    "AUTH"
            );
            throw new BusinessException("Authentication failed");
        }

        revokeToken(refreshToken);
        revokeAllUserTokensByType(users.getId(),JwtTokenType.ACCESS);

        String newRefreshToken = jwtService.generateRefreshToken(users);
        String newAccessToken = jwtService.generateToken(UserDetailsImpl.build(users));

        savedUserToken(users, newAccessToken, JwtTokenType.ACCESS);
        savedUserToken(users, newRefreshToken, JwtTokenType.REFRESH);

        auditLogService.registerEvent(
                users.getId(),
                "Refresh token processed successfully",
                "REFRESH_SUCCESS",
                "AUTH"
        );
        log.info("Refresh token processed successfully. userId={}", users.getId());

        long expiresInSeconds = accessExpirationMs / 1000;

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", expiresInSeconds);
    }


    @Transactional
    @Override
    public void logout(String bearerToken) {

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")){
            log.warn("Logout rejected due to invalid bearer token format");
            throw new BusinessException("Authentication failed");
        }

        String token = bearerToken.substring(7).trim();

        Optional<Token> storedToken = logoutRawToken(token);

        storedToken.ifPresent(t -> auditLogService.registerEvent(
                t.getUser().getId(),
                "Logout completed successfully",
                "LOGOUT_SUCCESS",
                "AUTH"
        ));
        log.info("Logout completed successfully");
    }


    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request, String userEmail, String ipAddress) {
        String normalizedEmail= normalizeEmail(normalizeEmail(userEmail));

        Users user = getUserByEmailOrThrow(normalizedEmail);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("Password change failed due to invalid current password. userId={}, ip={}",
                    user.getId(), ipAddress);
            auditLogService.registerEvent(user.getId(),
                    "Failed attempt to change password from IP: " + ipAddress,
                    "PASSWORD_CHANGE_FAILED",
                    "SECURITY");
            throw new BusinessException("Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("Password change rejected because new password matches current password. userId={}, ip={}",
                    user.getId(), ipAddress);
            auditLogService.registerEvent(
                    user.getId(),
                    "Password change rejected because new password matches current password from IP: " + ipAddress,
                    "PASSWORD_CHANGE_FAILED",
                    "SECURITY");

            throw new BusinessException("Authentication failed");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);

        revokeAllUserTokens(user.getId());

        String location = geoLocationService.getLocationFromIP(ipAddress);
        auditLogService.registerEvent(user.getId(),
                "Password change successful from IP: " + ipAddress + "(" + location + ")",
                "PASSWORD_CHANGED",
                "SECURITY");

        log.info("Password changed successfully. userId={}, ip={}", user.getId(), ipAddress);
    }


    @Transactional
    private Optional<Token>logoutRawToken(String token){
       return tokenRepository.findByToken(token).map(t -> {
            t.setRevoked(true);
            t.setExpired(true);
            return tokenRepository.save(t);
        });
    }

    @Transactional
    public void revokeAllUserTokens (Long userId){
        var tokens = tokenRepository.findByUser_IdAndExpiredFalseAndRevokedFalse(userId);
        tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true);});
        tokenRepository.saveAll(tokens);

        }

    @Transactional
    public void revokeToken(String rawToken){
        tokenRepository.findByToken(rawToken).ifPresent(t -> {
            t.setRevoked(true);
            t.setExpired(true);
            tokenRepository.save(t);
        });

    }


    @Transactional
    public void revokeAllUserTokensByType(Long userId, JwtTokenType jwtTokenType) {
        var tokens = tokenRepository.findByUser_IdAndJwtTokenTypeAndExpiredFalseAndRevokedFalse(userId, jwtTokenType);
        tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true);});
        tokenRepository.saveAll(tokens);
    }

    private RegisterResponseDto replayResponse(IdempotencyRecord record) {
        try {
            return objectMapper.readValue(record.getResponseBody(), RegisterResponseDto.class);
        } catch (Exception e) {
            throw new BusinessException("Couldn't replay idempotent create user response");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("Invalid credentials");
        }
        return email.trim().toLowerCase();
    }

    private String validateAndNormalizeIp(String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new BusinessException("IP address is required");
        }

        return ipAddress.trim();
    }

    private Users getUserByEmailOrThrow(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException("Invalid credentials"));
    }

    private void validateAccountLockStatus(Users user, String ipAddress) {
        if (!user.isAccountLocked()) {
            return;
        }
        if (isLockPeriodActive(user)) {
            auditLogService.registerEvent(
                    user.getId(),
                    "Blocked login attempt from IP: 129 ** ***",
                    "LOGIN_BLOCKED",
                    "AUTH"
            );

            log.warn("[LOGIN_BLOCKED] userId={} ip={}", user.getId(), ipAddress);

            throw new BusinessException("Authentication failed");
        }
        unlockUserAccount(user);
    }

    private boolean isLockPeriodActive(Users user) {
        return user.getLockTime() != null &&
                Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes() < 15;
    }

    private void unlockUserAccount(Users user) {
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockTime(null);

        usersRepository.save(user);

        log.info("[LOGIN_ACCOUNT_UNLOCKED] userId={}", user.getId());
    }

    private void validateUserIsActive(Users user, String ipAddress) {
        if (user.getUserStatus() == UserStatus.ACTIVE) {
            return;
        }

        auditLogService.registerEvent(
                user.getId(),
                "Login denied due to user status: " + user.getUserStatus()
                + " from IP: 129 **** ",
                "LOGIN_DENIED",
                "AUTH"
        );

        log.warn("[LOGIN_DENIED] reason=inactive_status userId={} status={} ip={}",
                user.getId(), user.getUserStatus(), ipAddress);

        throw new BusinessException("Authentication failed");
    }

    private Authentication authenticateUser(
            String normalizedEmail,
            String password,
            Users user,
            String ipAddress
    ) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail,password)
            );
        }catch (BadCredentialsException e) {
            handleFailedLoginAttempt(user,ipAddress);
            throw new BusinessException("Invalid credentials");
        }
    }

    private void handleFailedLoginAttempt(Users user, String ipAddress) {
        int attempts = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= user.getFailedLoginAttempts()) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
        }

        usersRepository.save(user);

        auditLogService.registerEvent(
                user.getId(),
                "Failed login attempt from IP: 129 ***",
                "LOGIN_FAILED",
                "AUTH"
        );

        log.warn("[LOGIN_FAILED] userId={} attempts={} ip={}",
                user.getId(), attempts, ipAddress);
    }

    private void validateIpIsNotSuspicious(Users user, String ipAddress) {
        boolean suspicious = ipAddressService.isSuspicious(ipAddress);

        if (!suspicious) {
            return;
        }

        auditLogService.registerEvent(
                user.getId(),
                "Login attempt from suspicious IP: " + ipAddress,
                "LOGIN_BLOCKED",
                "AUTH"
        );

        log.warn("[LOGIN_BLOCKED] reason= suspicious_ip userId={} ip={}",
                user.getId(), ipAddress);

        throw new BusinessException("Authentication failed");
    }

    private void resetFailedLoginAttempts(Users user) {
        if (user.getFailedLoginAttempts() == 0 && !user.isAccountLocked()) {
            return;
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);

        usersRepository.save(user);
    }

    private void registerLoginIp(Users user, String ipAddress) {
        IPAddressRequestDto dto = new IPAddressRequestDto();
        dto.setUserId(user.getId());
        dto.setIpAddress(ipAddress);

        ipAddressService.registerIP(dto);
    }


    private void registerSuccessfulLogin(Users user, String ipAddress) {
        String location = geoLocationService.getLocationFromIP(ipAddress);

        auditLogService.registerEvent(
                user.getId(),
                "Successful login from IP: " + ipAddress + " (" + location + ")",
                "LOGIN_SUCCESS",
                "AUTH"
        );

        log.info("[LOGIN_SUCCESS] userId={} ip={}", user.getId(), ipAddress);
    }

    private AuthTokens generateAndPersistTokens(Users user, UserDetails userDetails){
        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user.getId());
        savedUserToken(user, accessToken, JwtTokenType.ACCESS);
        savedUserToken(user, refreshToken, JwtTokenType.REFRESH);

        return new AuthTokens(accessToken, refreshToken);
    }

    private void validateEmailIsAvailable(String normalizedEmail) {
        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new
                    BusinessException("Registration failed");
        }
    }

    private Users buildPendingClientUser(RegisterRequestDto request, String normalizedEmail) {
        Users user = new Users();

        user.setName(request.getName().trim());
        user.setSurname(request.getSurname().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setDni(request.getDni().trim());
        user.setRegistrationDate(LocalDateTime.now());
        user.setRol(Rol.CLIENT);
        user.setUserStatus(UserStatus.PENDING_ACTIVATION);
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);

        return user;
    }

    private void registerSuccessfulRegistrationAudit(Users user) {
        auditLogService.registerEvent(
                user.getId(),
                "User registered successfully with status " + user.getUserStatus(),
                "REGISTER_SUCCESS",
                "AUTH"
        );
    }

    private RegisterResponseDto buildRegisterResponse(Users user) {
        return new RegisterResponseDto(
                "Registration completed successfully. Account activation pending",
                user.getUserStatus().name(),
                user.getEmail()
        );
    }
}
