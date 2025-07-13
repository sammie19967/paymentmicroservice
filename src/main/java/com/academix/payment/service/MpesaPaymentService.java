package com.academix.payment.service;

import com.academix.payment.dto.PaymentRequest;
import com.academix.payment.model.Transaction;
import com.academix.payment.model.enums.PaymentStatus;
import com.academix.payment.repository.TransactionRepository;
import com.academix.payment.util.MpesaUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class MpesaPaymentService {

    private final MpesaUtils mpesaUtils;
    private final TransactionRepository transactionRepository;

    public String initiatePayment(PaymentRequest request) {
        String timestamp = mpesaUtils.getTimestamp();
        String password = mpesaUtils.generatePassword(timestamp);
        String accessToken = mpesaUtils.generateAccessToken();

        // Save transaction with PENDING status
        Transaction transaction = Transaction.builder()
                .phoneNumber(request.getPhoneNumber())
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .reference("ZDS")
                .build();
        transaction = transactionRepository.save(transaction);

        // Build payload and send request
        Map<String, Object> payload = mpesaUtils.buildStkPushPayload(request, password, timestamp);
        HttpEntity<Map<String, Object>> entity = mpesaUtils.buildRequestEntity(payload, accessToken);
        String url = mpesaUtils.getStkPushUrl();

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            transaction.setMerchantRequestId((String) body.get("MerchantRequestID"));
            transaction.setCheckoutRequestId((String) body.get("CheckoutRequestID"));
            transactionRepository.save(transaction);

            return "Phone Number: " + request.getPhoneNumber() +
                    "\nAmount: " + request.getAmount() + " KES";
        } else {
            transaction.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(transaction);
            throw new RuntimeException("STK Push failed: " + response.getBody());
        }
    }
}
