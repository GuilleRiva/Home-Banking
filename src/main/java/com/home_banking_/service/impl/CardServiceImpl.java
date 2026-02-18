package com.home_banking_.service.impl;

import com.home_banking_.dto.response.CardResponseDto;
import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.CardMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Card;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.CardRepository;
import com.home_banking_.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardMapper cardMapper;

    public CardServiceImpl(CardRepository cardRepository, AccountRepository accountRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.cardMapper = cardMapper;
    }


    private String generateNumberCard(){
        Random random = new Random();
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            number.append(String.format("%04d", random.nextInt(10000)));
            if (i < 3) number.append(" ");
        }

        return number.toString();
    }


    private String generateCVV(){
        int cvv = new Random().nextInt(900) + 100;
        return String.valueOf(cvv);
    }


    @Override
    public CardResponseDto createCard(Long accountId, TypeCard typeCard, String mark) {
        log.info("Requesting card creation. accountId: {}, type: {}", accountId, typeCard);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Account not found"
                ));

        Card card = new Card();
        card.setAccount(account);
        card.setTypeCard(typeCard);
        card.setNumber(generateNumberCard());
        card.setCVV(generateCVV());
        card.setExpiration(LocalDateTime.now().plusYears(3));
        card.setStatusCard(StatusCard.ACTIVE);

        cardRepository.save(card);
        log.info("Card created.  accountId:{}, cardId={}", accountId, card.getId());
        return cardMapper.toDTO(card);
    }



    @Override
    public void cancelCard(Long cardId) {

        Card card = cardRepository.findById(cardId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "Card not found"
                ));

        if (card.getStatusCard()== StatusCard.BLOCKED){
            log.warn("Cancel card rejected: already blocked. cardId:{}",cardId);
            throw new BusinessException("Card is already blocked");
        }

        card.setStatusCard(StatusCard.BLOCKED);
        cardRepository.save(card);
        log.info("Card blocked successfully. ID: {}", cardId);

    }


    @Override
    public void deleteCard(Long cardId) {

        if (!cardRepository.existsById(cardId)){
            log.warn("Attempt to delete non-existent card. ID: {}", cardId);
            throw new ResourceNotFoundException("Card not found");
        }

        cardRepository.deleteById(cardId);
        log.info("Card deleted successfully. ID: {}", cardId);
    }


    @Transactional(readOnly = true)
    @Override
    public List<CardResponseDto> getCardByAccount(Long accountId) {
        log.info("Getting cards linked to account ID: {}", accountId);

        List<Card> cards = cardRepository.findByAccount_Id(accountId);

        log.debug("Total cards found for account {}: {}", accountId, cards.size());
        return cards.stream()
                .map(cardMapper::toDTO)
                .toList();
    }
}
