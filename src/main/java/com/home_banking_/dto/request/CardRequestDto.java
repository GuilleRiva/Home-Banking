package com.home_banking_.dto.request;

import com.home_banking_.enums.TypeCard;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO used to request creation of a new card associated with an account.")
public class CardRequestDto {

    @Schema(description = "Type of the card", example = "CREDIT", implementation = TypeCard.class)
    @NotNull(message = "type card is required")
    private TypeCard typeCard;

}
