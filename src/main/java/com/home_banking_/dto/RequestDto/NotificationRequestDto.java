package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequestDto {

    @NotBlank(message = "message is required")
    private String message;

    @NotBlank(message = "type notification is required")
    private String typeNotification;
}
