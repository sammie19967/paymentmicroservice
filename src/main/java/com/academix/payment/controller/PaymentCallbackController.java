package com.academix.payment.controller;

import com.academix.payment.model.Transaction;
import com.academix.payment.model.enums.PaymentStatus;
import com.academix.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentCallbackController {

    private final TransactionRepository transactionRepository;

    @PostMapping("/callback")
    @Transactional
    public ResponseEntity<String> handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("\uD83D\uDCE2 Received M-Pesa callback payload: {}", payload);

        try {
            Map<String, Object> body = (Map<String, Object>) payload.get("Body");
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");

            String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
            Integer resultCode = (Integer) stkCallback.get("ResultCode");
            String resultDesc = (String) stkCallback.get("ResultDesc");

            log.info("\uD83D\uDD0D Processing callback for CheckoutRequestID: {}, ResultCode: {}, ResultDesc: {}", checkoutRequestId, resultCode, resultDesc);

            Optional<Transaction> optionalTransaction = transactionRepository.findByCheckoutRequestId(checkoutRequestId);

            if (optionalTransaction.isPresent()) {
                Transaction transaction = optionalTransaction.get();

                transaction.setUpdatedAt(LocalDateTime.now());
                transaction.setDescription(resultDesc);

                if (resultCode == 0) {
                    transaction.setStatus(PaymentStatus.SUCCESS);

                    Map<String, Object> callbackMetadata = (Map<String, Object>) stkCallback.get("CallbackMetadata");
                    List<Map<String, Object>> items = (List<Map<String, Object>>) callbackMetadata.get("Item");

                    for (Map<String, Object> item : items) {
                        String name = (String) item.get("Name");
                        Object value = item.get("Value");

                        switch (name) {
                            case "MpesaReceiptNumber" -> transaction.setMpesaReceiptNumber((String) value);
                            case "TransactionDate" -> transaction.setTransactionTime(parseMpesaDate((Long) value));
                        }
                    }
                } else {
                    transaction.setStatus(PaymentStatus.FAILED);
                }

                transactionRepository.save(transaction);
                log.info("\u2705 Transaction updated in DB: {}", transaction);
            } else {
                log.warn("\u26A0\uFE0F No transaction found for CheckoutRequestID: {}", checkoutRequestId);
            }

        } catch (Exception e) {
            log.error("\u274C Error processing callback: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok("Callback received");
    }

    private LocalDateTime parseMpesaDate(Long mpesaTimestamp) {
        String ts = mpesaTimestamp.toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(ts, formatter);
    }
}
