package com.home_banking_.service;

import com.home_banking_.dto.request.DepositRequestDto;
import com.home_banking_.dto.request.TransactionRequestDto;
import com.home_banking_.dto.request.WithDrawRequestDto;
import com.home_banking_.dto.response.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto makeTransfer (TransactionRequestDto dto);

    TransactionResponseDto makeDeposit (DepositRequestDto dto);

    TransactionResponseDto makeWithdraw(WithDrawRequestDto dto);

    List<TransactionResponseDto> getMyTransactions();

    List<TransactionResponseDto> getTransactionsByUser(Long userId);

    List<TransactionResponseDto> getTransactionsByAccount(Long accountId);
}
