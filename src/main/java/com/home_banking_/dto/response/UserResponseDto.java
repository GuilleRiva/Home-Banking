package com.home_banking_.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.home_banking_.enums.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User data returned for administrative or internal use.")
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

    @Schema(description = "User role in the system.", example = "CLIENT", implementation = Rol.class)
    private String rol;

}
