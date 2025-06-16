package com.home_banking_.repository;

import com.home_banking_.model.SecurityToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityTokenRepository extends JpaRepository<SecurityToken, Long> {
}
