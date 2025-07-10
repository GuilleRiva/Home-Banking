package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;


@Data
@Schema(description = "DTO used to register a new user in the system.")
public class UserRequestDto {

    @Schema(description = "User's first name", example = "Juan")
    @NotBlank(message = "name is required")
    private String name;

    @Schema(description = "User's last name", example = "Pérez")
    @NotBlank(message = "surname is required")
    private String surname;

    @Schema(description = "User's email address", example = "juan.perez@example.com")
    @Email(message = "Email invalid format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Password for user authentication. Must be at least 8 characters.", example = "StrongPass123")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Schema(description = "User role in the system. Possible values: CLIENT, VENDEDOR", example = "CLIENT", implementation = Rol.class)
    private Rol rol;

}
