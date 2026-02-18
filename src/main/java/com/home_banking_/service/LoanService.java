package com.home_banking_.service;

import com.home_banking_.dto.request.LoanRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;

import java.util.Optional;

public interface LoanService {

    LoanResponseDto simulateLoans(LoanRequestDto dto);

    LoanResponseDto grantLoan (LoanRequestDto dto);

    Optional<LoanResponseDto> getLoanByAccount(Long accountId);
    
}
