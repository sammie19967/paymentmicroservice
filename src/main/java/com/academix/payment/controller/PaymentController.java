package com.academix.payment.controller;

import com.academix.payment.dto.PaymentRequest;
import com.academix.payment.service.MpesaPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final MpesaPaymentService mpesaPaymentService;

    @PostMapping("/stkpush")
    public ResponseEntity<String> initiateStkPush(@RequestBody PaymentRequest request) {
        mpesaPaymentService.initiateStkPush(request);
        return ResponseEntity.ok("STK push initiated");
    }
}
