package com.home_banking_.security.auth;

import com.home_banking_.dto.request.IPAddressRequestDto;
import com.home_banking_.dto.auth.ChangePasswordRequest;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.impl.JwtService;
import com.home_banking_.security.token.Token;
import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.token.TokenType;
import com.home_banking_.security.user.UserDetailsImpl;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.GeoLocationService;
import com.home_banking_.service.IPAddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public AuthResponse register(RegisterRequest request) {
        Users users = new Users();
        users.setName(request.getName());
        users.setSurname(request.getSurname());
        users.setEmail(request.getEmail());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setRol(request.getRol());

        usersRepository.save(users);

        String accessToken = jwtService.generateToken(UserDetailsImpl.build(users));
        String refreshToken = jwtService.generateRefreshToken(users);

        savedUserToken(users, accessToken);

        return new AuthResponse(accessToken, refreshToken, "BEARER");
    }


    private void savedUserToken(Users users, String token) {
        Token t = Token.builder()
                .user(users)
                .token(token)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(t);
    }


    @Transactional
    @Override
    public AuthResponse login(AuthRequest request, String ipAddress) {

        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1) Cuenta bloqueada
        if (user.isAccountLocked()) {
            if (Duration.between(user.getLockTime(), LocalDateTime.now()).toMinutes() < 15) {
                throw new BusinessException("Blocked account. Try later.");
            } else {
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                usersRepository.save(user);
            }
        }

        // 2) Autenticar credenciales
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 3) Reset de intentos fallidos
            user.setFailedLoginAttempts(0);
            usersRepository.save(user);

            // 4) IP sospechosa
            boolean isSuspicious = ipAddressService.isSuspicious(ipAddress);
            if (isSuspicious) {
                auditLogService.registerEvent(
                        user.getId(),
                        "Login attempt from suspicious IP: " + ipAddress,
                        "LOGIN_BLOCKED", "AUTH"
                );
                throw new BusinessException("This IP is marked as suspicious. Access denied.");
            }

            // 5) Registrar IP
            IPAddressRequestDto dto = new IPAddressRequestDto();
            dto.setId(String.valueOf(user.getId()));
            dto.setDirectionIP(ipAddress);
            ipAddressService.registerIP(dto);

            // 6) Generar tokens usando el principal (UserDetails)
            UserDetails ud = (UserDetails) auth.getPrincipal(); // ✅ clave
            String accessToken  = jwtService.generateToken(ud);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.error("ACCESS TOKEN startsWith={}", accessToken.substring(0, 25));
            log.error("REFRESH TOKEN startsWith={}", refreshToken.substring(0, 25));
            log.error("ACCESS exp={}, roles={}", jwtService.extractExpiration(accessToken),
                    jwtService.debugClaim(accessToken,"roles"));



            // 7) Revocar tokens previos y guardar el nuevo
            revokeAllUserTokens(user.getId());
            savedUserToken(user, accessToken);

            // 8) Auditoría login OK
            String location = geoLocationService.getLocationFromIP(ipAddress);
            auditLogService.registerEvent(
                    user.getId(),
                    "Successful login from IP: " + ipAddress + " (" + location + ")",
                    "LOGIN_SUCCESS",
                    "AUTH"
            );

            return new AuthResponse(accessToken, refreshToken, "Bearer");

        } catch (BadCredentialsException e) {
            // 9) Manejo de credenciales inválidas
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 3) {
                user.setAccountLocked(true);
                user.setLockTime(LocalDateTime.now());
            }
            usersRepository.save(user);

            auditLogService.registerEvent(
                    user.getId(),
                    "Failed login attempt with email: " + request.getEmail() + " from IP: " + ipAddress,
                    "LOGIN_FAILED", "AUTH"
            );
            throw new BusinessException("Invalid credentials");
        }
    }



    @Override
    public AuthResponse refreshToken(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        if (!jwtService.isTokenValid(refreshToken)){
            throw  new BusinessException("Invalid refresh token");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtService.generateToken(UserDetailsImpl.build(user));
        revokeAllUserTokens(user.getId());
        savedUserToken(user, newAccessToken);

        return new AuthResponse(newAccessToken, refreshToken, "Bearer");
    }



    @Override
    public void logout(String bearerToken) {

        if (bearerToken == null || !bearerToken.startsWith("Bearer")){
            throw new BusinessException("Invalid token format");
        }

        String token = bearerToken.substring(7).trim();

        logoutRawToken(token);
    }



    @Override
    public void changePassword(ChangePasswordRequest request, String userEmail, String ipAddress) {
        Users user = usersRepository.findByEmail(userEmail)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        //Verificar que la contraseña actual sea correcta
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.registerEvent(user.getId(),
                    "Failed attempt to change password from IP: " + ipAddress,
                    "PASSWORD_CHANGE_FAILED", "SECURITY");
            throw new BusinessException("Current password is incorrect");
        }

        //cambiar contraseña
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usersRepository.save(user);

        //revocar tokens previos
        revokeAllUserTokens(user.getId());

        String location = geoLocationService.getLocationFromIP(ipAddress);
        auditLogService.registerEvent(user.getId(),
                "Password change successful from IP:" + ipAddress + "(" + location + ")",
                "PASSWORD_CHANGED", "SECURITY");
    }



    private void logoutRawToken(String token){
        Optional<Token> storedToken = tokenRepository.findByToken(token);

        if (storedToken.isPresent()) {
            Token t = storedToken.get();
            t.setRevoked(true);
            t.setExpired(true);
            tokenRepository.save(t);
        }else {
            throw new BusinessException("Token not found");
        }
    }




    @Transactional
    public void revokeAllUserTokens (Long userId){
        var tokens = tokenRepository.findByUser_IdAndExpiredFalseAndRevokedFalse(userId);
        tokens.forEach(t -> { t.setExpired(true); t.setRevoked(true);});
        tokenRepository.saveAll(tokens);

        }
}
