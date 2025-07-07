package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    private Long id;
    private BigDecimal amount;
    private LocalDateTime creationDate;
    private String movementAccountType;
    private String typeTransaction;
    private String statusTransaction;
}
