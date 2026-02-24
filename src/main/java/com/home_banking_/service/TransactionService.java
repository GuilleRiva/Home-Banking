package com.home_banking_.service;

import com.home_banking_.dto.request.DepositRequestDto;
import com.home_banking_.dto.request.TransferRequestDto;
import com.home_banking_.dto.request.WithDrawRequestDto;
import com.home_banking_.dto.response.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto makeTransfer (TransferRequestDto dto);

    TransactionResponseDto makeDeposit (DepositRequestDto dto);

    TransactionResponseDto makeWithdraw(WithDrawRequestDto dto);

    List<TransactionResponseDto> getTransactionsByAccount(Long accountId);
    
    List<TransactionResponseDto> getMyTransactionsByAccount(Long accountId);

    List<TransactionResponseDto> getMyTransactions();

    List<TransactionResponseDto> getTransactionsByUser(Long userId);
}
