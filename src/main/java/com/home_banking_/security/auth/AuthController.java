package com.home_banking_.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints related to login, registration, and token management")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Create a new user in the database")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }


    @Operation(summary = "Login", description = "Authenticates the user and returns the access token and refresh token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody  AuthRequest request,
                                               HttpServletRequest httpRequest){

        String ipAddress = httpRequest.getRemoteAddr();
        AuthResponse response = authService.login(request, ipAddress);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "Refresh access token", description = "Returns a new access token from a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh (@RequestHeader("Authorization") String bearerToken){
        String refreshToken = bearerToken.replace("Bearer", "");

        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }


    @Operation(summary = "Logout ", description = "Revokes the user's current token (logout)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@RequestHeader("Authorization") String bearerToken){

        String token = bearerToken.replace("Bearer", "");
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
