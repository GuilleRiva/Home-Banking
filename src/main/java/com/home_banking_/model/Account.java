package com.home_banking_.model;
import com.home_banking_.enums.Currency;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 30)
    private String accountNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String alias;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "cbu", nullable = false, unique = true, length = 22)
    private String CBU;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "mask_alias", length = 100)
    private String maskAlias;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_account", nullable = false, length = 30)
    private TypeAccount typeAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_account", nullable = false, length = 30)
    private StatusAccount statusAccount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users users;

    @OneToMany(mappedBy = "accountOrigin",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    private List<Transaction> transactionOrigin= new ArrayList<>();

    @OneToMany(mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    private List<Loan> loans=new ArrayList<>();

    @OneToMany(mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    private List<Payment> payments=new ArrayList<>();

    @OneToMany(mappedBy = "account",
    cascade = CascadeType.ALL,
    orphanRemoval = true)
    private List<Card> cards= new ArrayList<>();


}
