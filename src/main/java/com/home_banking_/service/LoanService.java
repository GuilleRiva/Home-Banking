package com.home_banking_.service;

import com.home_banking_.dto.request.LoanRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;
import com.home_banking_.enums.LoanStatus;

import java.util.Optional;

public interface LoanService {

    LoanResponseDto simulateLoans(LoanSimulationRequestDto dto);

    LoanResponseDto grantLoan (String idempotencyKey, Long loanId) ;

    LoanResponseDto requestLoan(String idempotencyKey, LoanRequestDto dto);

    Optional<LoanResponseDto> getLoanByAccount(Long accountId);

    boolean existsByAccountIdAndStatusLoan(Long accountId, LoanStatus statusLoan);
    
}
