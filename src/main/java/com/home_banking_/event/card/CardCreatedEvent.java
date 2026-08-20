package com.home_banking_.event.card;

import com.home_banking_.enums.TypeCard;
import com.home_banking_.enums.audit.CardBrand;

public record CardCreatedEvent(
        Long cardId,
        Long recipientId,
        TypeCard typeCard,
        CardBrand cardBrand
) {
}
