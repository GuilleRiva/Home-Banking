package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IPAddressRequestDto {

    private String id;

    @NotBlank(message = "direction IP is required")
    private String directionIP;

    private String registrationDate;


}
