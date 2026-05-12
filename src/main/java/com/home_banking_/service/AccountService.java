package com.home_banking_.service;

import com.home_banking_.dto.request.AccountCreateRequestDto;
import com.home_banking_.dto.response.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponseDto createAccount(String idempotencyKey, AccountCreateRequestDto dto);

    List<AccountResponseDto> getAll();

    AccountResponseDto getAccountById(Long id);

    BigDecimal getBalance(Long accountId);

    AccountResponseDto getAccountByAlias(String alias);

    void deleteAccount(Long id);
}
