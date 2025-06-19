package com.home_banking_.service.impl;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Account;
import com.home_banking_.model.Card;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.CardRepository;
import com.home_banking_.service.CardService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardServiceImpl(CardRepository cardRepository, AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
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
        return String.format("%03d", new Random().nextInt(1000));
    }

    @Override
    public Card createCard(Long accountId, TypeCard typeCard, String mark) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        Card card = new Card();
        card.setAccount(account);
        card.setTypeCard(typeCard);
        card.setNumber(generateNumberCard());
        card.setCVV(generateCVV());
        card.setExpiration(LocalDateTime.now().plusYears(3));
        card.setStatusCard(StatusCard.ACTIVE);

        return cardRepository.save(card);
    }


    @Override
    public void cancelCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(()-> new ResourceNotFoundException("Card not found"));

        card.setStatusCard(StatusCard.BLOCKED);
        cardRepository.save(card);

    }


    @Override
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)){
            throw new ResourceNotFoundException("Card not found");
        }

        cardRepository.deleteById(cardId);
    }


    @Override
    public List<Card> getCardByAccount(Long accountId) {
        return cardRepository.findByAccount_Id(accountId);
    }
}
