package com.home_banking_.repository;

import com.home_banking_.enums.StatusCard;
import com.home_banking_.enums.TypeCard;
import com.home_banking_.model.Card;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByAccountId(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Card> findByIdAndAccountUsersEmail(Long cardId, String email);

    List<Card> findByAccountUsersEmail(String email);

    boolean existsByCardNumber(String cardNumber);

    boolean existsByAccountIdAndTypeCardAndStatusCardIn(
            Long accountId,
            TypeCard typeCard,
            Collection<StatusCard> statusCards
    );
}
