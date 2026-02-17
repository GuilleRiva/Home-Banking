package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.StatusLoan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoanRequestDto {

    @Schema(description = "Requested loan amount", example = "50000.00", minimum = "1000")
    @NotBlank(message = "amount is required")
    @DecimalMin("1000.00")
    private BigDecimal amount;

    @Schema(description = "Fees to pay", example = "1000")
    @NotBlank(message = "quotas are required ")
    private Integer quotas;

    @Schema(description = "ID of the account requesting the loan", example = "101")
    private Long accountId;

    @Schema(description = "Status loan", example = "Loan : 'owes three installments' ", implementation = StatusLoan.class)
    private StatusLoan statusLoan;


}
