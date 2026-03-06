package com.home_banking_.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO returned after successful authentication")
public class AuthResponse {

    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "Refresh token")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    private String tokenType= "Bearer";

    @Schema(description = "Access token expiration in seconds", example = "900")
    private long expiresIn;

}
