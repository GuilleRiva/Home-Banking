package com.home_banking_.controllers;

import com.home_banking_.enums.ServiceEntity;
import com.home_banking_.model.Payment;
import com.home_banking_.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Payment>>getPaymentByAccount(@PathVariable Long accountId){
        List<Payment> payments = paymentService.getPaymentByAccount(accountId);

        return ResponseEntity.ok(payments);
    }


    @GetMapping("/entity/{entity}")
    public ResponseEntity<List<Payment>> getPaymentByEntity(@PathVariable ServiceEntity entity){
        List<Payment> serviceEntities = paymentService.getPaymentByEntity(entity);

        return ResponseEntity.ok(serviceEntities);
    }


    @PostMapping
    public ResponseEntity<Payment> makePayment(@RequestParam Long accountId,
                                               @RequestParam BigDecimal amount,
                                               @RequestParam ServiceEntity serviceEntity,
                                               @RequestParam String description){

        Payment payment = paymentService.makePayment(accountId, amount, serviceEntity, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}
