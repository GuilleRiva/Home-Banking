package com.home_banking_.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home_banking_.enums.TypeNotification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.DefaultErrorStrategy;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    @Schema(description = "Unique identifier", example = "25")
    private Long id;

    @Schema(description = "Notification message content to be displayed to the user", example = "Your transaction was successful.")
    private String message;

    @Schema(description = "Notification about the shipping date", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime shippingDate;

    @Schema(description = "Indicates whether the message was read or not", example = "Read message")
    private Boolean read;

    @Schema(description ="Indicates the type of notification", example = "TRANSACCION_REALIZADA",
    implementation = TypeNotification.class)
    private TypeNotification typeNotification;
}
