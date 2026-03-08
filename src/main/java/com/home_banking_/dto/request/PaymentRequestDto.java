package com.home_banking_.dto.request;

import com.home_banking_.enums.Currency;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO used to request a new payment in the system.")
public class PaymentRequestDto {

    @Schema(description = "ID of the account from which the payment is made.", example = "1234567890")
    @NotNull(message = "Account ID is required")
    private Long accountId;

    @Schema(description = "The amount to be paid",example = "100.50")
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Digits(integer = 15, fraction = 2, message = "Amount must have up to 2 decimal places")
    private BigDecimal amount;

    @Schema(description = "The service entity receiving the payment", example = "ElectricityProvider",
    implementation = ServiceEntity.class)
    @NotNull(message = "Service entity is required")
    private ServiceEntity serviceEntity;

    @Schema(description = "A description or additional notes about the payment.", example = "Payment for electricity bill of June 2025")
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;



}
