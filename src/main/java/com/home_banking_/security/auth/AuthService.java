package com.home_banking_.security.auth;

public interface AuthService {

    AuthResponse register (RegisterRequest request);

    AuthResponse login (AuthRequest request, String ipAddress);

    AuthResponse refreshToken (String refreshToken);

    void logout (String bearerToken);
}
