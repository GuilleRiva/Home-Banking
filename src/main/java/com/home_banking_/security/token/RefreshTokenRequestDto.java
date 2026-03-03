package com.home_banking_.security.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequestDto {

    @Schema(description = "Refresh Token", example = "eJKLOasjkuTikjlsfdNVKC96...")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
