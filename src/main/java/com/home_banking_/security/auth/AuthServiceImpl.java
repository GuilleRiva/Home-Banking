package com.home_banking_.security.auth;

import com.home_banking_.dto.auth.*;
import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.enums.Rol;
import com.home_banking_.enums.JwtTokenType;
import com.home_banking_.enums.UserStatus;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.impl.JwtService;
import com.home_banking_.security.token.Token;
import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.user.UserDetailsImpl;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.GeoLocationService;
import com.home_banking_.service.IPAddressService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

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


    @Value("${application.security.jwt.expiration-ms:900000}")
    private long accessExpirationMs;

    @Override
    @Transactional
    public RegisterResponseDto register(RegisterRequestDto request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Registration failed");
        }

        Users users = new Users();
        users.setName(request.getName().trim());
        users.setSurname(request.getSurname().trim());
        users.setEmail(normalizedEmail);
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setDNI(request.getDni().trim());
        users.setRegistrationDate(LocalDateTime.now());
        users.setRol(Rol.CLIENT);
        users.setUserStatus(UserStatus.PENDING_ACTIVATION);
        users.setFailedLoginAttempts(0);
        users.setAccountLocked(false);
        users.setLockTime(null);

        usersRepository.save(users);

        auditLogService.registerEvent(
                users.getId(),
                "User registered successfully with status" + users.getUserStatus(),
                "REGISTER_SUCCESS",
                "AUTH"
        );

        return new RegisterResponseDto(
                "Registration completed successfully. Account activation pending.",
                users.getUserStatus().name(),
                users.getEmail()
        );
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
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Users user = usersRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        
        if (user.isAccountLocked()) {
            if (user.getLockTime() != null &&
                    Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes() < 15) {

                auditLogService.registerEvent(
                        user.getId(),
                        "Blocked login attempt from IP: " + ipAddress,
                        "LOGIN_BLOCKED",
                        "AUTH"
                );
                throw new BusinessException("Authentication failed");
            } else {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                usersRepository.save(user);
            }
        }
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("Authentication failed");
        }

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );
        } catch (BadCredentialsException e) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= 3) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }

            usersRepository.save(user);

            auditLogService.registerEvent(
                    user.getId(),
                    "Failed login attempt from IP: " + ipAddress,
                    "LOGIN_FAILED",
                    "AUTH"
            );

            throw new BusinessException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        usersRepository.save(user);

        boolean isSuspicious = ipAddressService.isSuspicious(ipAddress);
        if (isSuspicious) {
            auditLogService.registerEvent(
                    user.getId(),
                    "Login attempt from suspicious IP: " + ipAddress,
                    "LOGIN_BLOCKED",
                    "AUTH"
            );
            throw new BusinessException("Authentication failed");
        }

        IPAddressRequestDto dto = new IPAddressRequestDto();
        dto.setId(String.valueOf(user.getId()));
        dto.setDirectionIP(ipAddress);
        ipAddressService.registerIP(dto);

        UserDetails ud = (UserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateToken(ud);
        String refreshToken = jwtService.generateRefreshToken(user);

        revokeAllUserTokens(user.getId());
        savedUserToken(user, accessToken, JwtTokenType.ACCESS);
        savedUserToken(user, refreshToken, JwtTokenType.REFRESH);

        String location = geoLocationService.getLocationFromIP(ipAddress);
        auditLogService.registerEvent(
                user.getId(),
                "Successful login from IP: " + ipAddress + " (" + location + ")",
                "LOGIN_SUCCESS",
                "AUTH"
        );

        log.info("Successful authentication for userId={}", user.getId());

        return new AuthResponse(accessToken, refreshToken, "Bearer", accessExpirationMs / 1000);
    }

    @Transactional
    @Override
    public AuthResponse refreshToken(String refreshToken) {

        if (!jwtService.isTokenValid(refreshToken)) {
            throw new BusinessException("Invalid credentials");
        }

        Token storedToken = tokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new BusinessException("Invalid credentials"));

        if (storedToken.getJwtTokenType() != JwtTokenType.REFRESH) {
            throw new BusinessException("Invalid credentials");
        }
        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new BusinessException("authentication failed");
        }

        String email = jwtService.extractUsername(refreshToken);

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("authentication failed"));

        if (users.isAccountLocked()) {
            throw new BusinessException("authentication failed");
        }

        revokeToken(refreshToken);
        revokeAllUserTokensByType(users.getId(),JwtTokenType.ACCESS);

        String newRefreshToken = jwtService.generateRefreshToken(users);
        String newAccessToken = jwtService.generateToken(UserDetailsImpl.build(users));

        savedUserToken(users, newAccessToken, JwtTokenType.ACCESS);
        savedUserToken(users, newRefreshToken, JwtTokenType.REFRESH);

        long expiresInSeconds = accessExpirationMs / 1000;

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", expiresInSeconds);
    }

    @Transactional
    @Override
    public void logout(String bearerToken) {

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")){
            throw new BusinessException("Invalid credentials");
        }

        String token = bearerToken.substring(7).trim();

        logoutRawToken(token);
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request, String userEmail, String ipAddress) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("invalid credentials"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.registerEvent(user.getId(),
                    "Failed attempt to change password from IP: " + ipAddress,
                    "PASSWORD_CHANGE_FAILED", "SECURITY");
            throw new BusinessException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("authentication failed");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);

        revokeAllUserTokens(user.getId());

        String location = geoLocationService.getLocationFromIP(ipAddress);
        auditLogService.registerEvent(user.getId(),
                "Password change successful from IP:" + ipAddress + "(" + location + ")",
                "PASSWORD_CHANGED", "SECURITY");
    }

    @Transactional
    private void logoutRawToken(String token){

        tokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            t.setExpired(true);
            tokenRepository.save(t);
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
}
