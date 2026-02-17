package com.home_banking_.dto.ResponseDto;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {

    @Schema(description = "Unique card ID", example = "200")
    private Long id;

    @Schema(description = "Card number.", example = "2564367891230")
    private String number;

    @Schema(description = "Expiration date of the card", example = "2025-07-08T10:00:00")
    private LocalDateTime expiration;

    @Schema(description = "Type of the card ", example = "DEBITO",
    implementation = TypeCard.class)
    private TypeCard typeCard;

    @Schema(description = "Status of card", example = "Card expired",
    implementation = StatusCard.class)
    private StatusCard statusCard;

}
