package com.home_banking_.service;

import com.home_banking_.dto.request.CardCreatedRequestDto;
import com.home_banking_.dto.response.CardCreatedResponseDto;
import com.home_banking_.dto.response.CardResponseDto;
import jakarta.validation.Valid;

import java.util.List;

public interface CardService {

    CardCreatedResponseDto createMyCard(@Valid CardCreatedRequestDto request);

    CardCreatedResponseDto createCardForAccount(@Valid CardCreatedRequestDto request);

    void cancelCard(Long cardId);

    void blockMyCard(Long cardId);

    void cancelMyCard(Long cardId);

    void blockCard(Long cardId);

    List<CardResponseDto> getMyCards();

    List<CardResponseDto> getMyCardsByAccount(Long accountId);

    List<CardResponseDto> getCardsByAccount(Long accountId);

}
