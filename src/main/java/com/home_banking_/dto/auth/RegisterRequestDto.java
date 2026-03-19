package com.home_banking_.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "DTO used for user authentication")
public class RegisterRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be at most 50 characters")
    @Schema(description = "User first name", example = "Roman")
    private String name;

    @NotBlank(message = "Surname is required")
    @Size(max = 50, message = "Surname must be at most 50 characters")
    @Schema(description = "User surname", example = "Perez")
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 254, message = "Email must be at most 254 characters")
    @Schema(description = "User email", example = "user12345@mail.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8,max = 72, message = "Password must be between 8 and 72 characters")
    @Schema(description = "User password", example = "SafePassword1234")
    private String password;

    @NotBlank(message = "Dni is required")
    @Schema(description = "Dni", example = "83039691")
    private String dni;
}
