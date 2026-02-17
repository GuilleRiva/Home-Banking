package com.home_banking_.dto.ResponseDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.home_banking_.enums.StatusLoan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.DefaultErrorStrategy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanResponseDto {

    @Schema(description = "Unique identifier", example = "52")
    private Long id;

    @Schema(description = "Requested loan amount", example = "50000.00", minimum = "1000")
    @DecimalMin("1000.00")
    private BigDecimal amount;

    @Schema(description = "Fees to pay", example = "1000")
    private Integer quotas;

    @Schema(description = "percentage charged for the loan requested", example = "17%")
    private BigDecimal interestRate;

    @Schema(description = "total amount you must pay", example = "150000")
    private BigDecimal totalToPay;

    @Schema(description = "amount of the fee to be paid", example = "52686")
    private BigDecimal amountQuota;

    @Schema(description = "date from which you receive the loan", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;

    @Schema(description = "date on which you finish paying the last installment of the loan", example = "2025-07-08T14:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;

    @Schema(description = "current status of the loan", example = "CANCELED LOAN",
    implementation = StatusLoan.class)
    private StatusLoan statusLoan;
}
