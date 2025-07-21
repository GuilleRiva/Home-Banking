package com.home_banking_.security.auth;

import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Users;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.security.jwt.JwtService;
import com.home_banking_.security.token.Token;
import com.home_banking_.security.token.TokenRepository;
import com.home_banking_.security.token.TokenType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UsersRepository usersRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        Users users = new Users();
        users.setName(request.getName());
        users.setSurname(request.getSurname());
        users.setEmail(request.getEmail());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setRol(request.getRol());

        usersRepository.save(users);

        String accessToken = jwtService.generateToken(users);
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




    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Users user = usersRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        String accessToken = jwtService.generateToken((UserDetails) user);
        String refreshToken = jwtService.generateRefreshToken(user);


        revokeAllUserTokens(user);
        savedUserToken(user, accessToken);

        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }


    @Override
    public AuthResponse refreshToken(String refreshToken) {

        String email = jwtService.extractUsername(refreshToken);

        if (!jwtService.isTokenValid(refreshToken)){
            throw  new BusinessException("Invalid refresh token");
        }

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        savedUserToken(user, newAccessToken);

        return new AuthResponse(newAccessToken, refreshToken, "Bearer");
    }

    @Override
    public void logout(String token) {

        Token storedToken = tokenRepository.findByToken(token)
                .orElseThrow(()-> new ResourceNotFoundException("Token not found"));

        storedToken.setRevoked(true);
        storedToken.setExpired(true);
        tokenRepository.save(storedToken);

    }


    private void revokeAllUserTokens (Users user){
        List<Token> validTokens = tokenRepository.findByUser_IdAndExpiredFalseAndRevokedFalse(user.getId());

        validTokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });

        tokenRepository.saveAll(validTokens);
    }
}
