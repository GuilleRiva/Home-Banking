package com.home_banking_.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home_banking_.enums.TypeNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing a notification sent to a user")
public class NotificationResponseDto {

    @Schema(description = "Unique identifier", example = "25")
    private Long id;

    @Schema(description = "Notification message content displayed to the user", example = "Your transaction was successful.")
    private String message;

    @Schema(description = "Date and time when the notification was sent", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sentAt;

    @Schema(description = "Indicates whether the message was read or not", example = "true")
    private Boolean read;

    @Schema(description ="Type of the notification", example = "TRANSACCION_REALIZADA",
    implementation = TypeNotification.class)
    private TypeNotification typeNotification;
}
