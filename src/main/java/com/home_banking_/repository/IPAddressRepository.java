package com.home_banking_.repository;


import com.home_banking_.model.IPAddress;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface IPAddressRepository extends JpaRepository<IPAddress, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IPAddress> findByUsersId(Long userId);

    boolean existsByIpAndSuspiciousTrue(String ip);
}
