package com.home_banking_.repository;

import com.home_banking_.model.IPAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IPAddressRepository extends JpaRepository<IPAddress, Long> {

    List<IPAddress> findByUser(Long userId);
    boolean existsByDirectionIPAndSuspiciousTrue(String ip);
}
