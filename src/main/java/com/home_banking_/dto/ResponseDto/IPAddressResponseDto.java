package com.home_banking_.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class IPAddressResponseDto {

    @Schema(description = "Unique IP ID", example ="5263")
    private Long id;

    @Schema(description = "IP address to register or verify", example = "192.168.1.10")
    private String directionIP;

    @Schema(description = "Registration date of the IP address in ISO format", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registrationDate;

    @Schema(description = "Indicates whether the IP address is flagged as suspicious (true) or not (false)", example = "true")
    private boolean suspicious;
}
