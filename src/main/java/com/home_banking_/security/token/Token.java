package com.home_banking_.security.token;

import com.home_banking_.model.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "token", indexes = {
        @Index(name = "idx_token_user_valid", columnList = "user_id")
}, uniqueConstraints = @UniqueConstraint(name = "uq_token", columnNames = "token"))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private boolean revoked;
    @Column(nullable = false)
    private boolean expired;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Users user;
}
