package com.home_banking_.dto.response;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing a card with sensitive data masked")
public class CardResponseDto {

    @Schema(description = "Unique card ID", example = "200")
    private Long id;

    @Schema(description = "Masked card number", example = "**********************1234")
    private String numberMasked;

    @Schema(description = "Card expiration date in MM/YY format", example = "07/28")
    private String expiration;

    @Schema(description = "Type of the card ", example = "DEBIT",
    implementation = TypeCard.class)
    private TypeCard typeCard;

    @Schema(description = "Current status of the card", example = "ACTIVE",
    implementation = StatusCard.class)
    private StatusCard statusCard;

}
