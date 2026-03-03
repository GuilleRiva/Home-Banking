package com.home_banking_.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Schema( example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration in seconds", example = "900")
    private long expiresIn;

}
