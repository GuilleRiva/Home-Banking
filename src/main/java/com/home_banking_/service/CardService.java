package com.home_banking_.service;

import com.home_banking_.dto.ResponseDto.CardResponseDto;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.model.Card;

import java.util.List;

public interface CardService {

    CardResponseDto createCard(Long accountId, TypeCard typeCard, String mark);

    void cancelCard(Long cardId);

    void deleteCard(Long cardId);

    List<CardResponseDto> getCardByAccount(Long accountId);
}
