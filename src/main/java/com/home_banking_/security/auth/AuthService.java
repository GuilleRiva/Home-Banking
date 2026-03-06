package com.home_banking_.security.auth;

import com.home_banking_.dto.auth.AuthRequest;
import com.home_banking_.dto.auth.AuthResponse;
import com.home_banking_.dto.auth.ChangePasswordRequest;
import com.home_banking_.dto.auth.RegisterRequestDto;

public interface AuthService {

    AuthResponse register (RegisterRequestDto request);

    AuthResponse login (AuthRequest request, String ipAddress);

    AuthResponse refreshToken (String refreshToken);

    void logout (String bearerToken);

    void changePassword (ChangePasswordRequest request, String userEmail, String ipAddress);
}
