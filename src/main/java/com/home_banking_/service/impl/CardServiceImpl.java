package com.home_banking_.service.impl;

import com.home_banking_.dto.response.CardResponseDto;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.CardMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Card;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.CardRepository;
import com.home_banking_.service.CardService;
import com.home_banking_.service.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    public CardServiceImpl(CardRepository cardRepository, AccountRepository accountRepository, CardMapper cardMapper, CurrentUserService currentUserService) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.cardMapper = cardMapper;
        this.currentUserService = currentUserService;
    }


    @Override
    @Transactional
    public CardResponseDto createCard(Long accountId, TypeCard typeCard, String mark) {
        log.info("[CREATE_CARD_INIT] accountId={} typeCard={}", accountId, typeCard);

        Account account = getOwnedActiveAccountOrThrow(accountId);

        validateCardCreationRequest(typeCard, mark);
        validateCardCreationRules(account,typeCard);

        Card card = buildCard(account, typeCard, mark);
        Card savedCard = cardRepository.save(card);


        log.info("[CARD_CREATED_SUCCESS]   accountId={}, cardId={}, typeCard={}",
                account.getId(), savedCard.getId(), savedCard.getTypeCard());
        return cardMapper.toDTO(savedCard);
    }


    @Override
    @Transactional
    public void cancelCard(Long cardId) {
        log.info("[CARD_CANCEL_INIT] cardId={}", cardId);

        Card card = getOwnedCardOrThrow(cardId);

        validateCardCanBeCancelled(card);

        card.setStatusCard(StatusCard.CANCELLED);
        cardRepository.save(card);

        log.info("[CARD_CANCEL_SUCCESS] cardId={} accountId={} status={}",
                card.getId(), card.getAccount(), card.getStatusCard());
    }


    @Transactional(readOnly = true)
    @Override
    public List<CardResponseDto> getCardByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CARD_FETCH_BY_ID_INIT] account: {}", accountId);

        Account account = getOwnedAccount(accountId, email);

        List<CardResponseDto> cards = cardRepository.findByIdAndAccountUsersEmail(account.getId(),email)
                .stream()
                .map(cardMapper::toDTO)
                .toList();

        log.info("[CARD_FETCH_BY_ACCOUNT_SUCCESS] userEmail={} accountId={} cardCount={}",
                email, account.getId(), cards.size());

        return cards;
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

    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Account getOwnedActiveAccountOrThrow(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        Account account = accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        if (account.getStatusAccount() != StatusAccount.ACTIVE) {
            throw new BusinessException("Account is not active");
        }

        return account;
    }

    private Card getOwnedCardOrThrow(Long cardId) {
        String email = currentUserService.getCurrentUserEmail();

        return cardRepository.findByIdAndAccountUsersEmail(cardId, email)
                .orElseThrow(()-> new ResourceNotFoundException("card not found"));
    }


    private void validateCardCreationRequest(TypeCard typeCard,  String mark) {
        if (typeCard == null) {
            throw new BusinessException("Card type is required");
        }
        if (mark == null || mark.isBlank()) {
            throw new BusinessException("Card mark is required");
        }
    }

    private void validateCardCreationRules(Account account, TypeCard typeCard) {
        boolean alreadyHasCardType = cardRepository.existsByAccountIdAndTypeCard(
                account.getId(),
                typeCard
        );

        if (alreadyHasCardType) {
            throw new BusinessException("Account already has a card of this type");
        }
    }

    private void validateCardCanBeCancelled(Card card) {
        if (card.getStatusCard() == StatusCard.CANCELLED) {
            throw new BusinessException("Card is already cancelled");
        }
        if (card.getStatusCard() == StatusCard.BLOCKED) {
            throw new BusinessException("Blocked card cannot be cancelled directly");
        }
    }


    private Card buildCard(Account account, TypeCard typeCard, String mark) {
        Card card = new Card();
        card.setAccount(account);
        card.setTypeCard(typeCard);
        card.setNumber(generateNumberCard());
        card.setCVV(generateCVV());
        card.setExpiration(LocalDateTime.now().plusYears(3));
        card.setStatusCard(StatusCard.ACTIVE);

        return card;
    }

}
