package com.home_banking_.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {

    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String serviceEntity;
    private String statusPayment;
}
