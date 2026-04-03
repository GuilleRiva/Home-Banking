package com.home_banking_.service;

import com.home_banking_.dto.request.*;
import com.home_banking_.dto.response.TransactionResponseDto;

import java.util.List;

public interface TransactionService {

    TransactionResponseDto makeTransfer (TransactionRequestDto dto);

  /*  TransactionResponseDto makeDeposit (DepositRequestDto dto);*/

    TransactionResponseDto makeWithdraw(WithDrawRequestDto dto);

    TransactionResponseDto makeCustomerDeposit(CustomerDepositRequestDto dto);

    TransactionResponseDto makeAdministrativeCredit(AdministrativeCreditRequestDto dto);

    List<TransactionResponseDto> getMyTransactions();

    List<TransactionResponseDto> getTransactionsByUser(Long userId);

    List<TransactionResponseDto> getTransactionsByAccount(Long accountId);

}
