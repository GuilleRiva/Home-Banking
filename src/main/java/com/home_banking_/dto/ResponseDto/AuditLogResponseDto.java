package com.home_banking_.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogResponseDto {

    @Schema(description = "ID of the log entry", example = "1001")
    private Long id;

    @Schema(description = "Action performed", example = "TRANSFER")
    private String action;

    @Schema(description = "Detailed description of the action", example = "User transferred $2000.00 to account 1234")
    private String description;

    @Schema(description = "Date and time when action occurred", example = "2025-07-08T15:45:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dateTime;

    @Schema(description = "Origin IP address of the action", example = "192.168.0.1")
    private String ipOrigin;

    @Schema(description = "Type of action", example = "SECURITY")
    private String type;

}
