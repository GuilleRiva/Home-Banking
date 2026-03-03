package com.home_banking_.security.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Schema(example = "user@mail.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
  @Schema(example = "Password1234@")
  private String password;
}
