package com.home_banking_.dto.RequestDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "DTO used to register an audit log entry related to user actions or system events.")
public class AuditLogRequestDto {

    @Schema(description = "Action performed by the user or system", example = "TRANSFER")
    @NotBlank(message = "Action is required")
    @NotNull
    private String action;

    @Schema(description = "Detailed description of the event or operation", example = "User transferred $2000 to account 1234")
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Type or category of the log entry (e.g., SECURITY, OPERATION, SYSTEM",example = "SECURITY")
    @NotBlank(message = "Type is required")
    private String type;
}
