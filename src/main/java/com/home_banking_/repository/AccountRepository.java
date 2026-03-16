package com.home_banking_.repository;

import com.home_banking_.enums.TypeAccount;
import com.home_banking_.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAlias(String alias);

    boolean existsByUsersIdAndAliasIgnoreCase(String alias);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByCBU(String cbu);

    long countByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsByUserIdAndTypeAccount(Long userId, TypeAccount typeAccount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findByAliasAndUsersEmail(String alias, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id= :id and users.email =:email")
    Optional<Account> findByIdAndUsersEmail(@Param("id") Long id, @Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id and a.users.email =:email")
    Optional<Account> findByIdAndUsersEmailForUpdate(@Param("id") Long id, @Param("email") String email);
}
