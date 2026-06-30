package com.home_banking_.repository;

import com.home_banking_.enums.LoanStatus;
import com.home_banking_.model.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByAccountId(Long accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select l
    from Loan l
    join fetch l.account a
    where l.id = :loanId
""")
    Optional<Loan> findByIdForUpdate(@Param("loanId") Long loanId);

    boolean existsByAccountIdAndLoanStatusIn(Long accountId, List<LoanStatus> statuses);
}
