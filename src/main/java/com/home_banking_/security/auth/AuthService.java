package com.home_banking_.security.auth;

import com.home_banking_.dto.auth.ChangePasswordRequest;

public interface AuthService {

    AuthResponse register (RegisterRequest request);

    AuthResponse login (AuthRequest request, String ipAddress);

    AuthResponse refreshToken (String refreshToken);

    void logout (String bearerToken);

    void changePassword (ChangePasswordRequest request, String userEmail, String ipAddress);
}
