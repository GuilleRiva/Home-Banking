package com.home_banking_.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User profile returned for the currently authenticated user.")
public class UserProfileResponseDto {

    @Schema(description = "Unique identifier", example = "15")
    private Long id;

    @Schema(description = "User full name", example = "Juan Perez")
    private String fullName;

    @Schema(description = "User's email address", example = "juan.perez@example.com")
    private String email;

    @Schema(description = "User role in the system", example = "CLIENT")
    private String rol;
}
