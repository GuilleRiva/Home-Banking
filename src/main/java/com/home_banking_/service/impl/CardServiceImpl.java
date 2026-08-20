package com.home_banking_.service.impl;

import com.home_banking_.dto.request.CardCreatedRequestDto;
import com.home_banking_.dto.response.CardCreatedResponseDto;
import com.home_banking_.dto.response.CardResponseDto;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.enums.audit.CardBrand;
import com.home_banking_.event.card.CardCreatedEvent;
import com.home_banking_.exceptions.custom.BusinessException;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.CardMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Card;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.CardRepository;
import com.home_banking_.service.CardService;
import com.home_banking_.service.security.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private static final List<StatusCard> ACTIVE_OR_BLOCKED =
            List.of(StatusCard.ACTIVE, StatusCard.BLOCKED);

    private final SecureRandom secureRandom = new SecureRandom();

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardMapper cardMapper;
    private final CurrentUserService currentUserService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CardCreatedResponseDto createMyCard(@Valid CardCreatedRequestDto request) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CREATE_MY_CARD_INIT] userEmail={} accountId={} typeCard={} brand={}",
                email, request.getAccountId(), request.getTypeCard(), request.getBrand());

        Account account = getOwnedActiveAccountOrThrow(request.getAccountId(), email);

        validateCardCreationRules(account,request.getTypeCard());

        Card card = buildCard(account, request.getTypeCard(), request.getBrand());

        Card savedCard = cardRepository.save(card);

        eventPublisher.publishEvent(
                new CardCreatedEvent(
                        savedCard.getId(),
                        account.getUsers().getId(),
                        savedCard.getTypeCard(),
                        savedCard.getBrand()
                )
        );

        log.info("[CREATE_MY_CARD_SUCCESS] userEmail={} accountId={} cardId={} typeCard={} brand={}",
                email, account.getId(), savedCard.getId(), savedCard.getTypeCard(), savedCard.getBrand());

        return cardMapper.toCreatedResponseDto(savedCard);
    }

    @Override
    @Transactional
    public CardCreatedResponseDto createCardForAccount(@Valid CardCreatedRequestDto request) {
        log.info("[CREATE_CARD_FOR_ACCOUNT_INIT] accountId={} typeCard={} brand={}",
                request.getAccountId(), request.getTypeCard(), request.getBrand());

        Account account = getActiveAccountOrThrow(request.getAccountId());

        validateCardCreationRules(account, request.getTypeCard());

        Card card = buildCard(account, request.getTypeCard(),request.getBrand());

        Card savedCard = cardRepository.save(card);

        eventPublisher.publishEvent(
                new CardCreatedEvent(
                        savedCard.getId(),
                        account.getUsers().getId(),
                        savedCard.getTypeCard(),
                        savedCard.getBrand()
                )
        );

        log.info("[CREATE_CARD_FOR_ACCOUNT_SUCCESS] accountId={} cardId={} typeCard={} brand={}",
                account.getId(), savedCard.getId(), savedCard.getTypeCard(), savedCard.getBrand());

        return cardMapper.toCreatedResponseDto(savedCard);
    }


    @Override
    @Transactional
    public void cancelCard(Long cardId) {
        log.info("[CARD_CANCEL_INIT] cardId={}", cardId);

        Card card = getCardOrThrow(cardId);

        validateCardCanBeCancelled(card);

        card.setStatusCard(StatusCard.CANCELLED);
        cardRepository.save(card);

        log.info("[CARD_CANCEL_SUCCESS] cardId={} accountId={} status={}",
                card.getId(), card.getAccount(), card.getStatusCard());
    }

    @Override
    @Transactional
    public void blockMyCard(Long cardId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[BLOCK_MY_CARD_INIT] userEmail={} cardId={}", email, cardId);

        Card card = getOwnedCardOrThrow(cardId, email);

        validateCardCanBeBlocked(card);

        card.setStatusCard(StatusCard.BLOCKED);
        cardRepository.save(card);

        log.info("[BLOCKED_MY_CARD_SUCCESS] userEmail={} cardId={} status={}",
                email, card.getId(), card.getStatusCard());

    }

    @Override
    @Transactional
    public void cancelMyCard(Long cardId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CANCEL_MY_CARD_INIT] userEmail={} cardId={}", email, cardId);

        Card card = getOwnedCardOrThrow(cardId, email);

        validateCardCanBeCancelled(card);

        card.setStatusCard(StatusCard.CANCELLED);
        cardRepository.save(card);

        log.info("CANCEL_MY_CARD_SUCCESS] userEmail={} cardId={} status={}",
                email, card.getId(), card.getStatusCard());
    }

    @Override
    @Transactional
    public void blockCard(Long cardId) {

        log.info("[BLOCK_M_CARD_INIT] cardId={}", cardId);

        Card card = getCardOrThrow(cardId);

        validateCardCanBeBlocked(card);

        card.setStatusCard(StatusCard.BLOCKED);
        cardRepository.save(card);

        log.info("[BLOCK_MY_CARD_SUCCESS] cardId={} status={}",
                card.getId(), card.getStatusCard());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getMyCards() {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[GET_MY_CARDS_INIT] userEmail={}", email);

        List<CardResponseDto> cards = cardRepository.findByAccountUsersEmail(email)
                .stream()
                .map(cardMapper::toResponseDto)
                .toList();

        log.info("[GET_MY_CARDS_SUCCESS] userEmail={} cardCount={}", email, cards.size());

        return cards;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardResponseDto> getMyCardsByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[GET_MY_CARDS_BY_ACCOUNT_INIT] userEmail={} accountId={}", email, accountId);

        Account account = getOwnedActiveAccountOrThrow(accountId,email);

        List<CardResponseDto> cards = cardRepository
                .findByAccountIdAndAccountUsersEmail(account.getId(), email)
                .stream()
                .map(cardMapper::toResponseDto)
                .toList();

        log.info("[GET_MY_CARDS_BY_ACCOUNT_SUCCESS] userEmail={} accountId={} cardCount={}",
                email,account.getId(),cards.size());

        return cards;
    }


    @Transactional(readOnly = true)
    @Override
    public List<CardResponseDto> getCardsByAccount(Long accountId) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("[CARD_FETCH_BY_ID_INIT] account={}", accountId);

        Account account = getAccountOrThrow(accountId);

        List<CardResponseDto> cards = cardRepository.findByAccountId(account.getId())
                .stream()
                .map(cardMapper::toResponseDto)
                .toList();

        log.info("[CARD_FETCH_BY_ACCOUNT_SUCCESS]  accountId={} cardCount={}",
                 account.getId(), cards.size());

        return cards;
    }


    private String generateUniqueCardNumber() {
        String cardNumber;

        do {
            cardNumber = generateCardNumber();
        } while (cardRepository.existsByCardNumber(cardNumber));

        return cardNumber;
    }

    private String generateCardNumber() {
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            number.append(String.format("%04d", secureRandom.nextInt(10000)));

            if (i < 3) {
                number.append(" ");
            }
        }

        return number.toString();
    }


    private String generateCvv() {
        int cvv = secureRandom.nextInt(900) + 100;
        return String.valueOf(cvv);
    }


    private Account getOwnedActiveAccountOrThrow(Long accountId, String email) {
        Account account = getOwnedAccountOrThrow(accountId, email);

        validateAccountIsActive(account);

        return account;
    }

    private Account getOwnedAccountOrThrow(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Account getAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Card getOwnedCardOrThrow(Long cardId, String email) {
        return cardRepository.findByIdAndAccountUsersEmail(cardId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private Card getCardOrThrow(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private Account getActiveAccountOrThrow(Long accountId) {
        Account account = getAccountOrThrow(accountId);

        validateAccountIsActive(account);

        return account;
    }

    private void validateAccountIsActive(Account account) {
        if (account.getStatusAccount() != StatusAccount.ACTIVE) {
            throw new BusinessException("Account must be ACTIVE");
        }
    }



    private void validateCardCreationRules(Account account, TypeCard typeCard) {
        boolean alreadyHasActiveOrBlockedCard = cardRepository
                .existsByAccountIdAndTypeCardAndStatusCardIn(
                        account.getId(),
                        typeCard,
                        ACTIVE_OR_BLOCKED
                );

        if (alreadyHasActiveOrBlockedCard) {
            throw new BusinessException("Account already has an active or blocked card of this type");
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

    private void validateCardCanBeBlocked(Card card) {
        if (card.getStatusCard() == StatusCard.CANCELLED) {
            throw new BusinessException("Cancelled card cannot be blocked");
        }

        if (card.getStatusCard() == StatusCard.BLOCKED) {
            throw new BusinessException("Card is already blocked");
        }
    }


    private Card buildCard(Account account, TypeCard typeCard, CardBrand brand) {
        Card card = new Card();

        card.setAccount(account);
        card.setTypeCard(typeCard);
        card.setBrand(brand);
        card.setCardNumber(generateUniqueCardNumber());
        card.setCvv(generateCvv());
        card.setExpirationDate(LocalDateTime.now().plusYears(3));
        card.setStatusCard(StatusCard.ACTIVE);

        return card;
    }

}
