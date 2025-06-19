package com.home_banking_.service;

import com.home_banking_.model.Loan;

import java.math.BigDecimal;
import java.util.Optional;

public interface LoanService {

    Loan simulateLoans(BigDecimal amount, int quotas, BigDecimal interestRate);

    Loan grantLoan (Long amountId, BigDecimal amount, int quotas, BigDecimal interestRate);

    Optional<Loan> getLoanByAccount(Long accountId);
}
