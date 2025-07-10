package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "DTO used to create a new card associated with a user account.")
public class CardRequestDto {


    @Schema(description = "Card number in numeric format", example = "4562133789")
    @NotBlank(message = "number is required")
    private String number;


    @Schema(description = "Status of the card (e.g., CREDIT, DEBIT)", example = "CREDIT", implementation = TypeCard.class)
    @NotBlank(message = "type card is required")
    private TypeCard typeCard;


    @Schema(description = "Status of the card (e,g., ACTIVE, CANCELLED)", example = "ACTIVE", implementation = StatusCard.class)
    private StatusCard statusCard;
}
