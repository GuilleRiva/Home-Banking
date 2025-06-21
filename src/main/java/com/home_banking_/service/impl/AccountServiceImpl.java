package com.home_banking_.service.impl;

import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Account;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AccountService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;

    public AccountServiceImpl(AccountRepository accountRepository, UsersRepository usersRepository) {
        this.accountRepository = accountRepository;
        this.usersRepository = usersRepository;
    }


    @Override
    public Account createAccount(Account account, Long userId) {
        Users users= usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        account.setUsers(users);
        account.setCreationDate(LocalDateTime.now());
        account.setBalance(BigDecimal.ZERO);

        return accountRepository.save(account);
    }


    @Override
    public Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("account not found with ID: " + id));
    }


    @Override
    public List<Account> getAccountByUserId(Long userId) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        return users.getAccounts();
    }


    @Override
    public BigDecimal getBalance(Long accountId) {
        return getAccountById(accountId).getBalance();
    }


    @Override
    public Account getAccountByAlias(String alias) {
        return accountRepository.findByAlias(alias)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found with alias: " + alias));
    }


    @Override
    public void deleteAccount(Long id) {
        Account account = getAccountById(id);
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0){
            throw  new IllegalStateException("Account with balance greater than 0 cannot be deleted");
        }

        accountRepository.deleteById(id);
    }
}
