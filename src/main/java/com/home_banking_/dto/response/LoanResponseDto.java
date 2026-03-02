package com.home_banking_.dto.response;

import com.home_banking_.enums.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDto {

    @Schema(description = "Unique identifier", example = "52")
    private Long id;

    @Schema(description = "account ID to which the loan is credited")
    private Long accountId;

    @Schema(description = "Requested loan amount", example = "50000.00")
    private BigDecimal amount;

    @Schema(description = "Number of installments", example = "12")
    private Integer installments;

    @Schema(description = "Annual interest rate as decimal", example = "150000.00%")
    private BigDecimal interestRate;

    @Schema(description = "total amount to pay", example = "150000.00")
    private BigDecimal totalToPay;

    @Schema(description = "Installment amount", example = "52686.00")
    private BigDecimal installmentsAmount;

    @Schema(description = "loan start date", example = "2025-07-08T14:35:00")
    private LocalDateTime startDate;

    @Schema(description = "loan end date", example = "2025-07-08T14:35:00")
    private LocalDateTime endDate;

    @Schema(description = "current status of the loan", example = "ACTIVE")
    private LoanStatus statusLoan;
}
