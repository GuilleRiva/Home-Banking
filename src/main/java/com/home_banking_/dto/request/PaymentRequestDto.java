package com.home_banking_.dto.RequestDto;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "DTO used to request a new payment in the system.")
public class PaymentRequestDto {

    @Schema(description = "The amount to be paid, represented as a decimal value.", example = "100.50")
    private BigDecimal amount;

    @Schema(description = "The service entity receiving the payment (e.g., utility company, provider).", example = "ElectricityProvider",
    implementation = ServiceEntity.class)
    private ServiceEntity serviceEntity;

    @Schema(description = "The current status of the payment (e.g., PENDING, COMPLETED, FAILED).", example = "PENDING",
    implementation = StatusPayment.class)
    private StatusPayment statusPayment;

    @Schema(description = "A description or additional notes about the payment.", example = "Payment for electricity bill of June 2025")
    private String description;

    @Schema(description = "ID of the account from which the payment is made.", example = "1234567890")
    private Long accountId;

}
