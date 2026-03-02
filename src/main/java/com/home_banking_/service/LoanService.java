package com.home_banking_.service;

import com.home_banking_.dto.request.LoanGrantRequestDto;
import com.home_banking_.dto.request.LoanSimulationRequestDto;
import com.home_banking_.dto.response.LoanResponseDto;

import java.util.Optional;

public interface LoanService {

    LoanResponseDto simulateLoans(LoanSimulationRequestDto dto);

    LoanResponseDto grantLoan (LoanGrantRequestDto dto);

    Optional<LoanResponseDto> getLoanByAccount(Long accountId);
    
}
