package com.home_banking_.dto.request;

import com.home_banking_.enums.TypeCard;
import com.home_banking_.enums.audit.CardBrand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO used to request creation of a new card associated with an account.")
public class CardCreatedRequestDto {

    @NotNull(message = "Account id is required")
    private Long accountId;

    @Schema(description = "Type of the card", example = "CREDIT", implementation = TypeCard.class)
    @NotNull(message = "type card is required")
    private TypeCard typeCard;

    @NotNull(message = "Card brand is required")
    private CardBrand brand;

}
