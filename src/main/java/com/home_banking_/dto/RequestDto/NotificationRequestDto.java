package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationRequestDto {

    @NotBlank(message = "message is required")
    private String message;

    @NotBlank(message = "type notification is required")
    private String typeNotification;

    private String userId;
}
