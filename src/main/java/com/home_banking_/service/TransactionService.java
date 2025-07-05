package com.home_banking_.service;

import com.home_banking_.dto.RequestDto.TransactionRequestDto;
import com.home_banking_.dto.ResponseDto.TransactionResponseDto;
import com.home_banking_.model.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    TransactionResponseDto makeTransfer (TransactionRequestDto dto);

    List<TransactionResponseDto> getTransactionsByAccount(Long accountId);

    List<TransactionResponseDto> getTransactionsByUser(Long userId);

}
