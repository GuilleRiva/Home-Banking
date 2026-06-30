package com.home_banking_.dto.auth;

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

  @NotBlank(message = "Email is required")
  @Email(message = "Email must be valid")
  @Schema(example = "user@mail.com")
  private String email;

  @NotBlank(message = "Password is required")
  @Schema(example = "Password1234@")
  private String password;
}
