package com.home_banking_.dto.ResponseDto;

import com.home_banking_.model.Account;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private String CBU;
    private BigDecimal balance;
    private String alias;
    private String typeAccount;
    private String statusAccount;
    private LocalDateTime creationDate;

}
