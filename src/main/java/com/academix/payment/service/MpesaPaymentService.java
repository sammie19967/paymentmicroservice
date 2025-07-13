package com.academix.payment.service;

import com.academix.payment.dto.PaymentRequest;
import com.academix.payment.model.Transaction;
import com.academix.payment.model.enums.PaymentStatus;
import com.academix.payment.repository.TransactionRepository;
import com.academix.payment.util.MpesaUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaPaymentService {

    private final MpesaUtils mpesaUtils;
    private final TransactionRepository transactionRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // optional injection later

    public String initiateStkPush(PaymentRequest request) {
        try {
            String accessToken = mpesaUtils.generateAccessToken();

            // Generate timestamp and password
            String timestamp = mpesaUtils.getTimestamp();
            String password = mpesaUtils.generatePassword(timestamp);

            // Build STK push payload
            var payload = mpesaUtils.buildStkPushPayload(request, password, timestamp);

            // Send STK push request
            var response = restTemplate.postForEntity(
                    mpesaUtils.getStkPushUrl(),
                    mpesaUtils.buildRequestEntity(payload, accessToken),
                    String.class
            );

            log.info("STK Push response: {}", response.getBody());

            // Save transaction
            Transaction transaction = Transaction.builder()
                    .amount(request.getAmount())
                    .phoneNumber(request.getPhoneNumber())
                    .description(request.getDescription())
                    .status(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();

            transactionRepository.save(transaction);

            return "STK Push request sent successfully.";
        } catch (Exception e) {
            log.error("Failed to initiate STK Push: {}", e.getMessage());
            return "STK Push initiation failed.";
        }
    }
}
