package com.academix.payment.service;

import com.academix.payment.dto.PaymentRequest;
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

    public void initiateStkPush(PaymentRequest request) {
        // Get timestamp and generate password
        String timestamp = mpesaUtils.getTimestamp();
        String password = mpesaUtils.generatePassword(timestamp);

        // Get access token
        String accessToken = mpesaUtils.generateAccessToken();

        // Build STK push payload
        Map<String, Object> payload = mpesaUtils.buildStkPushPayload(request, password, timestamp);

        // Build HTTP entity
        HttpEntity<Map<String, Object>> entity = mpesaUtils.buildRequestEntity(payload, accessToken);

        // Get STK push URL
        String url = mpesaUtils.getStkPushUrl();

        // Send STK push request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        // Handle response
        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("✅ STK Push sent successfully");
        } else {
            System.err.println("❌ STK Push failed: " + response.getBody());
            throw new RuntimeException("STK Push failed");
        }
    }
}
