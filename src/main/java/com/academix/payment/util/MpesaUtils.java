package com.academix.payment.util;

import com.academix.payment.config.MpesaConfig;
import com.academix.payment.dto.PaymentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpesaUtils {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAccessToken() {
        String credentials = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodedCredentials = Base64Utils.encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                mpesaConfig.getAccessTokenUrl(),
                HttpMethod.GET,
                request,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        } else {
            throw new RuntimeException("Failed to retrieve access token");
        }
    }

    public String getTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public String generatePassword(String timestamp) {
        String toEncode = mpesaConfig.getBusinessShortCode() + mpesaConfig.getPasskey() + timestamp;
        return Base64Utils.encodeToString(toEncode.getBytes(StandardCharsets.UTF_8));
    }

    public Map<String, Object> buildStkPushPayload(PaymentRequest request, String password, String timestamp) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("BusinessShortCode", mpesaConfig.getBusinessShortCode());
        payload.put("Password", password);
        payload.put("Timestamp", timestamp);
        payload.put("TransactionType", "CustomerPayBillOnline");
        payload.put("Amount", request.getAmount());
        payload.put("PartyA", request.getPhoneNumber());
        payload.put("PartyB", mpesaConfig.getBusinessShortCode());
        payload.put("PhoneNumber", request.getPhoneNumber());
        payload.put("CallBackURL", mpesaConfig.getCallbackUrl());
        payload.put("AccountReference", "ZDS");
        payload.put("TransactionDesc", request.getDescription());
        return payload;
    }

    public HttpEntity<Map<String, Object>> buildRequestEntity(Map<String, Object> payload, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(payload, headers);
    }

    public String getStkPushUrl() {
        return mpesaConfig.getStkPushUrl();
    }
}
