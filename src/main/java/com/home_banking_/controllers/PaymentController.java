package com.home_banking_.controllers;

import com.home_banking_.dto.request.PaymentRequestDto;
import com.home_banking_.dto.request.ServicePaymentRequestDto;
import com.home_banking_.dto.response.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name= "Payment Controller", description = "Operations related to user payments and services entities.")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private static final String IDEMPOTENCY_KEY_HEADER= "Idempotency_Key";


    @Operation(
            summary = "Get payments by account ID",
            description = "Retrieves a list of all payments associated with the specified account ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = PaymentResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "Account not found or no payments available")
    })
    @GetMapping("/account/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED', 'AUDITOR')")
    public ResponseEntity<List<PaymentResponseDto>>getPaymentByAccount(
            @Parameter(name = "accountId", description = "Unique identifier of the account", required = true)
            @PathVariable Long accountId){

        log.info("GET /api/payments/account/{} - Getting payments associated with the account", accountId);

        List<PaymentResponseDto> payments = paymentService.getPaymentByAccount(accountId);
        log.info("Payments recovered: {} by the account {}", payments.size(), accountId);

        return ResponseEntity.ok(payments);
    }


    @Operation(
            summary = "Get payments by service entity",
            description = "Retrieves a list of payments filtered by the specified service entity(e.g., INTERNET, GAS, WATER)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PaymentResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "No payments found for the given service entity")
    })
    @GetMapping("/entity/{entity}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED', 'AUDITOR')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentByEntity(
            @Parameter(name = "entity", description = "Service entity to filter payments by", required = true,
                    schema = @Schema(implementation = ServiceEntity.class, example = "INTERNET"))
            @PathVariable ServiceEntity entity){

        log.info("GET /api/payments/entity/{} - Getting paid by service entity", entity);

        List<PaymentResponseDto> serviceEntities = paymentService.getPaymentByEntity(entity);
        log.info("Payments found for the entity {}: {}", entity, serviceEntities.size());

        return ResponseEntity.ok(serviceEntities);
    }


    @Operation(
            summary = "Make a payment",
            description = "Registers a new payment transaction in the system based on the provided request data."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment processed successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PaymentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment request")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT')")
    public ResponseEntity<PaymentResponseDto> makePayment(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody PaymentRequestDto dto){
        log.info("POST /api/payments - Processing payment for account ID: {}, entity: {}",
                dto.getAccountId(), dto.getServiceEntity());

        PaymentResponseDto payment = paymentService.makePayment(idempotencyKey, dto);
        log.info("Payment successfully made. Payment ID: {}", payment.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }


    @PostMapping("/services")
    @PreAuthorize("hasAnyRole('CLIENT')")
    public ResponseEntity<PaymentResponseDto> payService(
            @RequestHeader(IDEMPOTENCY_KEY_HEADER) String idempotencyKey,
            @Valid @RequestBody ServicePaymentRequestDto dto
            ) {
        log.info("[PAYMENT_SERVICE_REQUEST] accountId={} serviceEntity={}",
                dto.getAccountId(), dto.getServiceEntity());

        PaymentResponseDto payment = paymentService.payService(idempotencyKey, dto);
        log.info("[PAYMENT_SERVICE_RESPONSE] paymentId={} accountId={}",
                payment.getId(), dto.getAccountId());

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
