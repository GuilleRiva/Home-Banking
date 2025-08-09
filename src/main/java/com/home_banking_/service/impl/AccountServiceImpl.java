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
import com.home_banking_.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final AccountMapper accountMapper;
    private final AuditLogService auditLogService;



    @Override
    public AccountResponseDto createAccount(AccountRequestDto dto) {
        log.info("Creating a new account for user with ID: {}", dto.getUserId());

        Users users = usersRepository.findById(dto.getUserId())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Account account= accountMapper.toEntity(dto);
        account.setBalance(BigDecimal.ZERO);
        account.setCreationDate(LocalDateTime.now());
        account.setUsers(users);
        accountRepository.save(account);

        log.info("Account created successfully with ID: {}", account.getId());
        return accountMapper.toDto(account);
    }



    @Override
    public List<AccountResponseDto> getAll() {
        log.info("Getting all registered accounts");

        List<AccountResponseDto> accounts = accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .toList();

        log.debug("Total accounts found: {}", accounts.size());
        return accounts;
    }




    @Override
    public AccountResponseDto getAccountById(Long id) {
        log.info("Searching account ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(()-> {
                    log.warn("Account not found with ID: {}", id);
                    return new ResourceNotFoundException("Account not found");
                });

        log.info("Account not found with ID: {}", id);
        return accountMapper.toDto(account);
    }




    @Override
    public BigDecimal getBalance(Long accountId) {
        log.info("Getting account balance with ID: {}",accountId);

      Account account = accountRepository.findById(accountId)
              .orElseThrow(()-> {
                  log.warn("Account not found when checking balance. ID: {}",accountId);
                  return new ResourceNotFoundException("Account not found");
              });

      log.debug("Current account balance {}: {}", account, account.getBalance());
      return account.getBalance();
    }




    @Override
    public AccountResponseDto getAccountByAlias(String alias) {
        String maskedAlias = maskAlias(alias);
        log.info("Searching account with alias: {}", alias);

       Account account = accountRepository.findByAlias(alias)
               .orElseThrow(()-> {
                   log.warn("Account not found with alias: {}", alias);
                   return new ResourceNotFoundException("Account not found with alias" + alias);
               });

       log.info("Account found with alias: {}", alias );
       return accountMapper.toDto(account);
    }




    @Override
    public void deleteAccount(Long id) {
        log.info("Account deletion request with ID: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(()-> {
                    log.warn("Account not found when trying to delete. ID: {}",id);
                    return new ResourceNotFoundException("Account not found");
                });

        accountRepository.delete(account);
        log.info("Account successfully deleted with ID: {}", id);


        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Account with alias'" + account.getAlias() + "' deleted",
                "DELETE_ACCOUNT",
                "BANKING"
        );

    }

    private String maskAlias(String alias) {
        if (alias == null || alias.length() < 4) return "****";
        return alias.substring(0,2) + "****" + alias.substring(alias.length() - 2);
    }
}
