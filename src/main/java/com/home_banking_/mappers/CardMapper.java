package com.home_banking_.mappers;

import com.home_banking_.dto.response.CardCreatedResponseDto;
import com.home_banking_.dto.response.CardResponseDto;
import com.home_banking_.model.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "accountId", source = "account.id")
    @Mapping(target = "maskedCardNumber", expression = "java(maskCardNumber(card.getCardNumber()))")
    CardResponseDto toResponseDto(Card card);

    @Mapping(target = "accountId", source = "account.id")
    CardCreatedResponseDto toCreatedResponseDto(Card card);

    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String lastFour = cardNumber.replace(" ", "")
                .substring(cardNumber.replace(" ", "").length() - 4);

        return "**** **** **** " + lastFour;
    }
}
