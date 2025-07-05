package com.home_banking_.service.impl;

import com.home_banking_.dto.RequestDto.TransactionRequestDto;
import com.home_banking_.dto.ResponseDto.TransactionResponseDto;
import com.home_banking_.enums.StatusTransaction;
import com.home_banking_.enums.TypeTransaction;
import com.home_banking_.exceptions.BusinessException;
import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.mappers.TransactionMapper;
import com.home_banking_.model.Account;
import com.home_banking_.model.Transaction;
import com.home_banking_.model.Users;
import com.home_banking_.repository.AccountRepository;
import com.home_banking_.repository.TransactionRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final UsersRepository usersRepository;

    public TransactionServiceImpl(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper, UsersRepository usersRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.usersRepository = usersRepository;
    }

    @Override
    public TransactionResponseDto makeTransfer(TransactionRequestDto dto) {
        Account origin = accountRepository.findById(dto.getAccountOriginId())
                .orElseThrow(()-> new ResourceNotFoundException("Origin account not found"));

        Account destination = accountRepository.findById(dto.getAccountDestinyId())
                .orElseThrow(()-> new ResourceNotFoundException("Destination account not found"));

        BigDecimal amount = dto.getAmount();

        if (origin.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Insufficient balance for transfer");
        }

        origin.setBalance(origin.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountRepository.save(origin);
        accountRepository.save(destination);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setAccountOrigin(origin);
        transaction.setAccountDestiny(destination);
        transaction.setCreationDate(LocalDateTime.now());
        transaction.setStatusTransaction(StatusTransaction.COMPLETED);
        transaction.setTypeTransaction(TypeTransaction.COBRO);

        transactionRepository.save(transaction);

        return transactionMapper.toDto(transaction);

    }

    @Override
    public List<TransactionResponseDto> getTransactionsByAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(()-> new ResourceNotFoundException("Account not found"));

        List<Transaction> transactions = transactionRepository.findByAccountOrigin_Users_IdOrAccountDestiny_Users_Id(accountId, accountId);

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<TransactionResponseDto> getTransactionsByUser(Long userId) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        List<Transaction> transactions = transactionRepository.findByAccountOrigin_Users_IdOrAccountDestiny_Users_Id(userId, userId);

        return transactions.stream()
                .map(transactionMapper::toDto)
                .collect(Collectors.toList());

    }
}
