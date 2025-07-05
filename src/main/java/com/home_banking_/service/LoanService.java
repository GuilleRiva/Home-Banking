package com.home_banking_.service;

import com.home_banking_.dto.RequestDto.LoanRequestDto;
import com.home_banking_.dto.ResponseDto.LoanResponseDto;
import com.home_banking_.model.Loan;

import java.math.BigDecimal;
import java.util.Optional;

public interface LoanService {

    LoanResponseDto simulateLoans(LoanRequestDto dto);

    LoanResponseDto grantLoan (LoanRequestDto dto);

    Optional<LoanResponseDto> getLoanByAccount(Long accountId);
    
}
