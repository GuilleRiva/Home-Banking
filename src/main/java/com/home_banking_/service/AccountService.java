package com.home_banking_.service;

import com.home_banking_.dto.request.AccountCreateRequestDto;
import com.home_banking_.dto.response.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponseDto createAccount(String idempotencyKey, AccountCreateRequestDto dto);

    List<AccountResponseDto> getAll();

    AccountResponseDto getAccountByIdForAdmin(Long id);

    BigDecimal getAccountBalanceByIdForAdmin(Long accountId);

    AccountResponseDto getAccountByAliasForAdmin(String alias);

    void deleteAccount(Long id);

    AccountResponseDto getMyAccountById(Long accountId);

    BigDecimal getMyAccountBalance(Long accountId);

    AccountResponseDto getMyAccountByAlias(String alias);
}
