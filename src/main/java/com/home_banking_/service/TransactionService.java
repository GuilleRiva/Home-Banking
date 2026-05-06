package com.home_banking_.service;

import com.home_banking_.dto.request.*;
import com.home_banking_.dto.response.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto makeTransfer (String idempotencyKey, TransactionRequestDto dto);


    TransactionResponseDto makeWithdraw(String idempotencyKey, WithDrawRequestDto dto);

    TransactionResponseDto makeCustomerDeposit(String idempotencyKey, CustomerDepositRequestDto dto);

    TransactionResponseDto makeAdministrativeCredit(String idempotencyKey, AdministrativeCreditRequestDto dto);

    List<TransactionResponseDto> getMyTransactions();

    List<TransactionResponseDto> getTransactionsByUser(Long userId);

    List<TransactionResponseDto> getTransactionsByAccount(Long accountId);

}
