package com.home_banking_.dto.request;

import com.home_banking_.enums.TypeNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO used to create a new notification for a user.")
public class NotificationRequestDto {


    @Schema(description = "Notification message content to be displayed to the user", example = "Your transaction was successful.")
    @Size(max = 255, message = "Message must not exceed 255 characters")
    @NotBlank(message = "message is required")
    private String message;

    @Schema(description = "Type of notification", example = "INFO", implementation = TypeNotification.class)
    @NotNull(message = "type notification is required")
    private TypeNotification typeNotification;

}
