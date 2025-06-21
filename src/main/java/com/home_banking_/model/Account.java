package com.home_banking_.model;
import com.home_banking_.enums.StatusAccount;
import com.home_banking_.enums.TypeAccount;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String alias;
    private BigDecimal balance;
    private String CBU;
    private LocalDateTime creationDate;

    @Enumerated(EnumType.STRING)
    private TypeAccount typeAccount;

    @Enumerated(EnumType.STRING)
    private StatusAccount statusAccount;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users users;


    @OneToMany(mappedBy = "accountOrigin")
    private List<Transaction> transactionOrigin;

    @OneToMany(mappedBy = "accountDestiny")
    private List<Transaction> transactionDestiny;

    @OneToMany(mappedBy = "account")
    private List<Loan> loans;

    @OneToMany(mappedBy = "account")
    private List<Payment> payments;

    @OneToMany(mappedBy = "account")
    private List<Card> cards;


}
