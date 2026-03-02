package com.home_banking_.service.impl;

import com.home_banking_.dto.request.LoanGrantRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.enums.LoanStatus;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.LoanMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Loan;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.service.LoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class LoanServiceImpl implements LoanService {

    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(AccountRepository accountRepository, LoanRepository loanRepository, LoanMapper loanMapper) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.loanMapper = loanMapper;
    }


    @Transactional(readOnly = true)
    @Override
    public LoanResponseDto simulateLoans(LoanSimulationRequestDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Account account = accountRepository.findByIdAndUsersEmail(dto.getAccountId(), email)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "Account not found"
                ));

        Loan simulatedLoan = buildLoanFromDto(dto.getAmount(), dto.getInstallments(), account);
        simulatedLoan.setInstallments(null);

        log.info("Simulation completed for accountId: {} | Total to pay: {}",
                dto.getAccountId(), simulatedLoan.getTotalToPay());

        return loanMapper.toDto(simulatedLoan);
    }



    @Transactional
    public LoanResponseDto grantLoan(LoanGrantRequestDto dto) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Account account = accountRepository.findByIdAndUsersEmail(dto.getAccountId(), email)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Account not found"
                ));

        Loan loan = buildLoanFromDto(dto.getAmount(), dto.getInstallments(), account);
        loan.setStatusLoan(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(LocalDateTime.now().plusMonths(dto.getInstallments()));

        // desembolso minimo
        account.setBalance(account.getBalance().add(dto.getAmount()));

        loanRepository.save(loan);
        log.info("Loan successfully granted. AccountId: {} | Total to pay: {} | End date: {}",
                dto.getAccountId(), loan.getTotalToPay(), loan.getEndDate());

        return loanMapper.toDto(loan);
    }

    private Loan buildLoanFromDto(BigDecimal amount, Integer installments, Account account) {
        BigDecimal interestRate = new BigDecimal("0.20");
        BigDecimal totalToPay = amount.add(amount.multiply(interestRate));
        BigDecimal installmentAmount = totalToPay.divide(BigDecimal.valueOf(installments), 2, RoundingMode.HALF_UP);

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setAmount(amount);
        loan.setInstallments(installments);
        loan.setInterestRate(interestRate);
        loan.setTotalToPay(totalToPay);
        loan.setInstallmentsAmount(installmentAmount);
        return loan;
    }


    @Transactional(readOnly = true)
    @Override
    public Optional<LoanResponseDto> getLoanByAccount(Long accountId) {

        Optional<Loan> loan = loanRepository.findByAccountId(accountId);

        if (loan.isPresent()){
            log.info("Loan found for ID account: {} | Total to pay: {}", accountId, loan.get().getTotalToPay());
        }else {
            log.warn("No active loan found for account ID: {}",accountId);
        }

        return loan.map(loanMapper::toDto);
    }

}
