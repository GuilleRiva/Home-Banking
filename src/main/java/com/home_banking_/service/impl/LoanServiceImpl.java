package com.home_banking_.service.impl;

import com.home_banking_.enums.StatusLoan;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Account;
import com.home_banking_.model.Loan;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.service.LoanService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;

    public LoanServiceImpl(AccountRepository accountRepository, LoanRepository loanRepository) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
    }


    @Override
    public Loan simulateLoans(BigDecimal amount, int quotas, BigDecimal interestRate) {
        BigDecimal interest = amount.multiply(interestRate);
        BigDecimal total = amount.add(interest);
        BigDecimal MonthlyQuota = total.divide(BigDecimal.valueOf(quotas), 2 , RoundingMode.HALF_UP);

        Loan simulation = new Loan();
        simulation.setAmount(String.valueOf(amount));
        simulation.setQuotas(String.valueOf(quotas));
        simulation.setInterestRate(interestRate);
        simulation.setTotalToPay(total);
        simulation.setStatusLoan(StatusLoan.SIMULADO);
        simulation.setStartDate(LocalDateTime.now());

        return simulation;
    }


    @Override
    public Loan grantLoan(Long amountId, BigDecimal amount, int quotas, BigDecimal interestRate) {
        Account account = accountRepository.findById(amountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new BusinessException("The loan amount must be greater than 0");
        }

        Loan loan = simulateLoans(amount, quotas, interestRate);
        loan.setAccount(account);
        loan.setStatusLoan(StatusLoan.EN_CURSO);
        loan.setStartDate(LocalDateTime.now());


        //credit the money
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return loanRepository.save(loan);
    }


    @Override
    public Optional<Loan> getLoanByAccount(Long accountId) {
        return loanRepository.findByAccountId(accountId);
    }
}
