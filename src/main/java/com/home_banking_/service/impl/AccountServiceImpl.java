package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.AccountRequestDto;
import com.home_banking_.dto.ResponseDto.AccountResponseDto;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.AccountMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final AccountMapper accountMapper;



    @Override
    public AccountResponseDto createAccount(AccountRequestDto dto) {
        Users users = usersRepository.findById(dto.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Account account= accountMapper.toEntity(dto);
        account.setBalance(BigDecimal.ZERO);
        account.setCreationDate(LocalDateTime.now());
        account.setUsers(users);
        accountRepository.save(account);

        return accountMapper.toDto(account);
    }

    @Override
    public List<AccountResponseDto> getAll() {
        return accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .toList();
    }


    @Override
    public AccountResponseDto getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        return accountMapper.toDto(account);
    }




    @Override
    public BigDecimal getBalance(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        return account.getBalance();
    }


    @Override
    public AccountResponseDto getAccountByAlias(String alias) {
       Account account = accountRepository.findByAlias(alias)
               .orElseThrow(()-> new ResourceNotFoundException("Account not found with alias" + alias));

       return accountMapper.toDto(account);
    }


    @Override
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException( "Account not found "));

        accountRepository.delete(account);

    }
}
