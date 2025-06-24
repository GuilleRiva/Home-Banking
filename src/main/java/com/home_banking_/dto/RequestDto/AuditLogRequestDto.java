package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuditLogRequestDto {

    @NotBlank(message = "action is required")
    @NotNull
    private String action;

    @NotBlank(message = "description is required")
    private String description;

    @NotBlank(message = "type is required")
    private String type;
}
