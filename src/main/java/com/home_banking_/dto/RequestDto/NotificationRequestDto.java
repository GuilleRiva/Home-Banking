package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.TypeNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "DTO used to create a new notification for a user.")
public class NotificationRequestDto {


    @Schema(description = "Notification message content to be displayed to the user", example = "Your transaction was successful.")
    @NotBlank(message = "message is required")
    private String message;

    @Schema(description = "Type or category of the notification (e.g., INFO, WARNING, ALERT)", example = "INFO", implementation = TypeNotification.class)
    @NotBlank(message = "type notification is required")
    private TypeNotification typeNotification;

    @Schema(description = "ID of the user to whom the notification is assigned", example = "1001")
    private Long userId;
}
