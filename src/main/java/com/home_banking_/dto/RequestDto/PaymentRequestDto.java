package com.home_banking_.dto.RequestDto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentRequestDto {

    private BigDecimal amount;
    private String serviceEntity;
    private String description;
    private Long accountId;
}
