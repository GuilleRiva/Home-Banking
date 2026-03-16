package com.home_banking_.service.impl;

import com.home_banking_.dto.request.AccountCreateRequestDto;
import com.home_banking_.dto.response.AccountResponseDto;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.AccountMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.LoanRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.AccountService;
import com.home_banking_.service.AuditLogService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UsersRepository usersRepository;
    private final AccountMapper accountMapper;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;
    private final LoanRepository loanRepository;

    private static final int MAX_ACCOUNTS_PER_USER = 3;
    private static final int MIN_ALIAS_LENGTH = 6;
    private static final int MAX_ALIAS_LENGTH = 20;

    @Transactional
    @Override
    public AccountResponseDto createAccount(AccountCreateRequestDto dto) {
        String email = currentUserService.getCurrentUserEmail();

        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateAlias(dto.getAlias());
        validateAccountCreationRules(users.getId(), dto);

        Account account = accountMapper.toEntity(dto);
        account.setUsers(users);
        account.setBalance(BigDecimal.ZERO);
        account.setCreationDate(LocalDateTime.now());
        account.setStatusAccount(StatusAccount.ACTIVE);
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setCBU(generateUniqueCbu());

        accountRepository.save(account);

        return accountMapper.toDto(account);
    }

    @Override
    public List<AccountResponseDto> getAll() {

        List<AccountResponseDto> accounts = accountRepository.findAll()
                .stream()
                .map(accountMapper::toDto)
                .toList();

        log.debug("Total accounts found: {}", accounts.size());
        return accounts;
    }

    @Override
    public AccountResponseDto getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "Account not found"
                ));
        return accountMapper.toDto(account);
    }

    @Override
    public BigDecimal getBalance(Long accountId) {

      Account account = accountRepository.findById(accountId)
              .orElseThrow(()-> new ResourceNotFoundException(
                      "Account not found"
              ));

      log.debug("Current account balance {}: {}", account, account.getBalance());
      return account.getBalance();
    }

    @Override
    public AccountResponseDto getAccountByAlias(String alias) {
        String maskedAlias = maskAlias(alias);
        log.info("Searching account with alias: {}", maskedAlias);

       Account account =getOwnedAccountByAlias(alias, maskedAlias);

       return accountMapper.toDto(account);
    }

    @Transactional
    @Override
    public void deleteAccount(Long id) {
        String email = currentUserService.getCurrentUserEmail();

        log.info("Account closure request with ID: {}", id);

        Account account = accountRepository.findByAliasAndUsersEmail(String.valueOf(id),email)
                .orElseThrow(()-> new ResourceNotFoundException( "Account not found"));

        validateAccountCanBeClosedOrDeleted(account);
        account.setStatusAccount(StatusAccount.CLOSED);

        accountRepository.save(account);
        log.info("Account successfully deleted with ID: {}", id);

        auditLogService.registerEvent(
                account.getUsers().getId(),
                "Account closed successfully. accountId=" + account.getId() + ", alias=" + account.getAlias(),
                "CLOSE_ACCOUNT",
                "BANKING"
        );
        log.info("Account successfully closed with ID: {}", id);
    }
    // HELPERS //
    private void validateAlias(String alias) {
        if (alias == null || alias.isBlank()) {
            throw new BusinessException("Alias is required");
        }
        String trimmedAlias = alias.trim();

        if (trimmedAlias.length() < MIN_ALIAS_LENGTH || trimmedAlias.length() > MAX_ALIAS_LENGTH) {
            throw new BusinessException("Alias length must be between " + MIN_ALIAS_LENGTH + "and" + MAX_ALIAS_LENGTH + "characters");
        }
    }

    private void validateAccountCreationRules(Long userId, AccountCreateRequestDto dto) {
        if (dto.getTypeAccount() == null) {
            throw new BusinessException("Account type is required");
        }
        if (accountRepository.existsByUsersIdAndAliasIgnoreCase(dto.getAlias())) {
            throw new BusinessException("Alias already in use");
        }

        if (accountRepository.countByUserId(userId) >= MAX_ACCOUNTS_PER_USER) {
            throw new BusinessException("User has reached the maximum number of accounts");
        }

        if (accountRepository.existsByUserIdAndTypeAccount(userId, dto.getTypeAccount())) {
            throw new BusinessException("User already has an account of this type");
        }
    }

    private void validateAccountCanBeClosedOrDeleted(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Account cannot be deleted while balance is greater than zero");
        }
    }

    private Account getOwnedAccount(Long accountId, String email) {
        return accountRepository.findByIdAndUsersEmail(accountId, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private Account getOwnedAccountByAlias(String alias, String email) {
        return accountRepository.findByAliasAndUsersEmail(alias, email)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));
    }

    private String generateUniqueAccountNumber() {
        String number;
        do {
            number = String.valueOf(ThreadLocalRandom.current()
                    .nextLong(100000000L, 999999999L));
        } while (accountRepository.existsByAccountNumber(number));

        return number;
    }

    private String generateUniqueCbu(){
        String cbu;
        do {
            cbu = String.valueOf(ThreadLocalRandom.current()
                    .nextLong(1000000000000000000L, 999999999999999999L));
        }while (accountRepository.existsByCBU(cbu));

        return cbu;
    }

    private String maskAlias(String alias) {
        if (alias == null || alias.length() < 4) return "****";
        return alias.substring(0,2) + "****" + alias.substring(alias.length() - 2);
    }
}
