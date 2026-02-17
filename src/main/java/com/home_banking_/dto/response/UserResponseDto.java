package com.home_banking_.dto.ResponseDto;

import com.home_banking_.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    @Schema(description = "Unique identifier", example = "15")
    private Long id;

    @Schema(description = "User's first name", example = "Juan")
    private String name;

    @Schema(description = "User's last name", example = "Pérez")
    private String surname;

    @Schema(description = "User's email address", example = "juan.perez@example.com")
    @Email(message = "Email invalid format")
    private String email;


    @Schema(description = "User register date", example = "2025-07-08T14:35:00")
    private LocalDateTime registrationDate;

    @Schema(description = "User role in the system. Possible values: CLIENT, VENDEDOR", example = "CLIENT", implementation = Rol.class)
    private String rol;

}
