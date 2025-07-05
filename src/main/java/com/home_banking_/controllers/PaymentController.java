package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.PaymentRequestDto;
import com.home_banking_.dto.ResponseDto.PaymentResponseDto;
import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<PaymentResponseDto>>getPaymentByAccount(@PathVariable Long accountId){
        List<PaymentResponseDto> payments = paymentService.getPaymentByAccount(accountId);

        return ResponseEntity.ok(payments);
    }


    @GetMapping("/entity/{entity}")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentByEntity(@PathVariable ServiceEntity entity){
        List<PaymentResponseDto> serviceEntities = paymentService.getPaymentByEntity(entity);

        return ResponseEntity.ok(serviceEntities);
    }


    @PostMapping
    public ResponseEntity<PaymentResponseDto> makePayment(@Valid @RequestBody PaymentRequestDto dto){

        PaymentResponseDto payment = paymentService.makePayment(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
