package com.home_banking_.service;

import com.home_banking_.enums.TypeCard;
import com.home_banking_.model.Card;

import java.util.List;

public interface CardService {

    Card createCard(Long accountId, TypeCard typeCard, String mark);

    void cancelCard(Long cardId);

    void deleteCard(Long cardId);

    List<Card> getCardByAccount(Long accountId);
}
