package com.home_banking_.dto.RequestDto;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CardRequestDto {

    @NotBlank(message = "number is required")
    private String number;


    @NotBlank(message = "type card is required")
    private String typeCard;

}
