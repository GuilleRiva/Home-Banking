package com.home_banking_.repository;

import com.home_banking_.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

List<Transaction> findByAccountOrigin_IdOrAccountDestiny_Id(Long originId, Long destinyId);

List<Transaction> findByAccountOrigin_User_IdOrAccountDestiny_User_Id(Long userIdOrigin, Long userIdDestiny);
}
