package com.home_banking_.dto.response;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.enums.audit.CardBrand;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO representing a card with sensitive data masked")
public class CardCreatedResponseDto {

    @Schema(description = "Unique card ID", example = "200")
    private Long id;

    private String cardNumber;

    private String cvv;

    @Schema(description = "Card expiration date in MM/YY format", example = "07/28")
    private LocalDateTime expirationDate;

    @Schema(description = "Current status of the card", example = "ACTIVE",
            implementation = StatusCard.class)
    private StatusCard statusCard;

    @Schema(description = "Account ID is required")
    private Long accountId;

    @Schema(description = "Card type is required", example = "DEBIT",
    implementation = TypeCard.class)
    private TypeCard typeCard;

    @Schema(description = "Card brand is required", example = "MASTERCARD",
    implementation = CardBrand.class)
    private CardBrand brand;


}
