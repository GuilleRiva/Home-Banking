package com.home_banking_.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO used to register or handle IP address information in the system.")
public class IPAddressRequestDto {

    @Schema(description = "Unique identifier of the IP entry (usually auto/generated)", example = "a8f3c9e1-1234-4567-abcd-789ef4560001")
    private String id;


    @Schema(description = "IP address to register or verify", example = "192.168.1.10")
    @NotBlank(message = "Direction IP is required")
    private String directionIP;

    @Schema(description = "Registration date of the IP address in ISO format", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime registrationDate;



}
