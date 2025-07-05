package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IPAddressRequestDto {

    private String userId;

    @NotBlank(message = "direction IP is required")
    private String directionIP;


}
