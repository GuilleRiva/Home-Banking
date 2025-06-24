package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogResponseDto {
    private Long id;
    private String action;
    private String description;
    private LocalDateTime dateTime;
    private String ipOrigin;
    private String type;

}
