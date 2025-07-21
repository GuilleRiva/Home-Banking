package com.home_banking_.security.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register (@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }


    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login (@RequestBody @Valid AuthRequest request){
        return ResponseEntity.ok(authService.login(request));
    }


    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh (@RequestHeader("Authorization") String bearerToken){
        String refreshToken = bearerToken.replace("Bearer", "");

        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }


    @Operation(summary = "Logout (revoke current token)")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout (@RequestHeader("Authorization") String bearerToken){

        String token = bearerToken.replace("Bearer", "");
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}
