package com.home_banking_.repository;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.model.Account;
import com.home_banking_.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByAccount_Id(Long accountId);

    List<Payment> findByServiceEntity(ServiceEntity entity);

}
