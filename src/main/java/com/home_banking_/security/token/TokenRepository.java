package com.home_banking_.security.token;

import com.home_banking_.enums.JwtTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    List<Token> findByUser_IdAndExpiredFalseAndRevokedFalse(Long userId);

    Optional<Token> findByToken(String token);

    boolean existsByTokenAndExpiredFalseAndRevokedFalse(String token);

    List<Token> findByUser_IdAndJwtTokenTypeAndExpiredFalseAndRevokedFalse(Long userId, JwtTokenType jwtTokenType);

}
