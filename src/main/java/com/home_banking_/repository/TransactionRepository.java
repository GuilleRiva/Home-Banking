package com.home_banking_.repository;

import com.home_banking_.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            select t FROM Transaction t
            where t.accountOrigin.users.id = :userId
            or t.accountDestiny.users.id = :userId
            """)
List<Transaction> findAllByUserId(@Param("userId") Long userId);
    
@Query("""
        select t from Transaction t
        where t.accountOrigin.users.email = :email
        or t.accountDestiny.users.email = :email
        order by t.creationDate desc
        """)
List<Transaction> findMyTransactions(@Param("email") String email);

@Query("""
        select t from Transaction t
        where (t.accountOrigin.id = :accountId or t.accountDestiny.id = :accountId)
        order by t.creationDate desc
        """)
List<Transaction> findByAccountId(@Param("accountId") Long accountId);

@Query("""
        select t from Transaction t
        where (t.accountOrigin.id = :accountId or t.accountDestiny.id = :accountId)
        and (t.accountOrigin.users.email = :email or t.accountDestiny.users.email = :email)
        order by t.creationDate desc
        """)
List<Transaction> findMyTransactionsByAccount(@Param("email") String email,
                                              @Param("accountId") Long accountId);

}
