package com.home_banking_.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @Schema(description = "User email", example = "client@gmail.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "User password", example = "123456")
    private String password ;
}
