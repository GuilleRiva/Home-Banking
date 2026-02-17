package com.home_banking_.dto.ResponseDto;

import com.home_banking_.enums.MovementAccountType;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.enums.StatusPayment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDto {

    @Schema(description = "Unique identifier", example = "52")
    private Long id;

    @Schema(description = "A description or additional notes about the payment.", example = "Payment for electricity bill of June 2025")
    private String description;

    @Schema(description = "The amount to be paid, represented as a decimal value.", example = "100.50")
    private BigDecimal amount;

    @Schema(description = "date the payment was made", example = "2025-07-08T14:35:00")
    private LocalDateTime paymentDate;

    @Schema(description = "The service entity receiving the payment (e.g., utility company, provider).", example = "ElectricityProvider",
    implementation = ServiceEntity.class)
    private ServiceEntity serviceEntity;

    @Schema(description = "The current status of the payment (e.g., PENDING, COMPLETED, FAILED).", example = "PENDING",
    implementation = StatusPayment.class)
    private StatusPayment statusPayment;

    @Schema(description = "Movement account type", example = "CUENTA_EXTERNA",
    implementation = MovementAccountType.class)
    private MovementAccountType movementAccount;
}
