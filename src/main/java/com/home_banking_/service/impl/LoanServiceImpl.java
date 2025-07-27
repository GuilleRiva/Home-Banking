package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.LoanRequestDto;
import com.home_banking_.dto.ResponseDto.LoanResponseDto;
import com.home_banking_.enums.StatusLoan;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.LoanMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Loan;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.service.LoanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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


    @Override
    public LoanResponseDto simulateLoans(LoanRequestDto dto) {
        log.info("Simulating a loan for an ID account: {} | Amount requested : {} | quotas: {}",
                dto.getAccountId(), dto.getAmount(), dto.getQuotas());

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(()-> {
                        log.warn("Account not found when simulating loan. ID: {}", dto.getAccountId());
                        return new ResourceNotFoundException( "Account not found");
                        });

        Loan simulatedLoan = buildLoanFromDto(dto, account);

        log.info("Simulation completed for account ID: {} | Total to pay: {}",
                dto.getAccountId(), simulatedLoan.getTotalToPay());

        return loanMapper.toDto(simulatedLoan);
    }



    @Override
    public LoanResponseDto grantLoan(LoanRequestDto dto) {
        log.info("Granting a loan to an ID account: {} | Amount: {} | Quotas: {}",
                dto.getAccountId(), dto.getAmount(), dto.getQuotas());

        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(()-> {
                    log.warn("Account not found when granting loan. ID: {}", dto.getAccountId());
                    return new ResourceNotFoundException("Account not found");
                });

        Loan loan = buildLoanFromDto(dto, account);
        loan.setStatusLoan(StatusLoan.EN_CURSO);
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(LocalDateTime.now().plusMonths(Long.parseLong(String.valueOf(dto.getQuotas()))));

        loanRepository.save(loan);
        log.info("Loan successfully granted. Account ID: {} | Total to pay: {} | End date: {}",
                dto.getAccountId(), loan.getTotalToPay(), loan.getEndDate());

        return loanMapper.toDto(loan);
    }


    @Override
    public Optional<LoanResponseDto> getLoanByAccount(Long accountId) {
        log.info("Looking for active loan for ID account: {}", accountId);

        Optional<Loan> loan = loanRepository.findByAccountId(accountId);

        if (loan.isPresent()){
            log.info("Loan found for ID account: {} | Total to pay: {}", accountId, loan.get().getTotalToPay());
        }else {
            log.warn("No active loan found for account ID: {}",accountId);
        }

        return loan.map(loanMapper::toDto);
    }


    private Loan buildLoanFromDto(LoanRequestDto dto, Account account){
        BigDecimal amount = new BigDecimal(String.valueOf(dto.getAmount()));
        BigDecimal interestRate = BigDecimal.valueOf(0.20);
        BigDecimal totalToPay = amount.add(amount.multiply(interestRate));
        BigDecimal quotaAmount = totalToPay.divide(new BigDecimal(dto.getQuotas()), 2, RoundingMode.HALF_UP);

        Loan loan = new Loan();
        loan.setAccount(account);
        loan.setAmount(dto.getAmount());
        loan.setQuotas(dto.getQuotas());
        loan.setInterestRate(interestRate);
        loan.setTotalToPay(totalToPay);
        loan.setAmountQuota(quotaAmount);

        return loan;
    }
}
