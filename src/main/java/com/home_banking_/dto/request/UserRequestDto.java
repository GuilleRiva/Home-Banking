package com.home_banking_.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Schema(description = "DTO used to register a new user in the system.")
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserRequestDto {

    @NotBlank
    @Size(min = 2, max = 50)
    private String name;

    @NotBlank
    @Size(min = 2,max = 50)
    private String surname;

    @Email
    @NotBlank
    @Size(max = 254)
    private String email;

    @Size(min = 10, max = 72, message = "Password must be between 10 and 72 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[^\\\\w\\\\s]).+$",
            message = "Password must include uppercase, lowercase, number, and symbol"
    )
    @ToString.Exclude
    private String password;

}
