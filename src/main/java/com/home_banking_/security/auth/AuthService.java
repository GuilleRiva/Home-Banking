package com.home_banking_.security.auth;

import com.home_banking_.dto.auth.*;

public interface AuthService {

    RegisterResponseDto register (RegisterRequestDto request);

    AuthResponse login (AuthRequest request, String ipAddress);

    AuthResponse refreshToken (String refreshToken);

    void logout (String bearerToken);

    void changePassword (ChangePasswordRequest request, String userEmail, String ipAddress);
}
