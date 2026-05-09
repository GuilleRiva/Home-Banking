package com.home_banking_.repository;

import com.home_banking_.enums.LoanStatus;
import com.home_banking_.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    Optional<Loan> findByAccountId(Long accountId);

    Optional<Loan> findByAccountIdAndAccountUsersEmail(Long accountId, String email);

    boolean existsByAccountIdAndStatusLoan(Long id, LoanStatus loanStatus);
}
