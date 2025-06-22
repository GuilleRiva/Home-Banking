package com.home_banking_.service;

import com.home_banking_.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    Account createAccount(Account account, Long userId);

    List<Account> getAll();

    Account getAccountById(Long id);

    List<Account> getAccountByUserId(Long userId);

    BigDecimal getBalance(Long accountId);

    Account getAccountByAlias(String alias);

    void deleteAccount(Long id);
}
