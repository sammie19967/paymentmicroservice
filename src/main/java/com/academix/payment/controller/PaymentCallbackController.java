package com.academix.payment.controller;

import com.academix.payment.model.Transaction;
import com.academix.payment.model.enums.PaymentStatus;
import com.academix.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
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
            if (payload == null || !payload.containsKey("Body")) {
                throw new IllegalArgumentException("Invalid callback payload format");
            }

            Map<String, Object> body = (Map<String, Object>) payload.get("Body");
            Map<String, Object> stkCallback = (Map<String, Object>) body.get("stkCallback");

            if (stkCallback == null) {
                throw new IllegalArgumentException("Missing stkCallback data");
            }

            String checkoutRequestId = (String) stkCallback.get("CheckoutRequestID");
            Integer resultCode = (Integer) stkCallback.get("ResultCode");
            String resultDesc = (String) stkCallback.get("ResultDesc");

            if (checkoutRequestId == null || resultCode == null) {
                throw new IllegalArgumentException("Missing required callback parameters");
            }

            log.info("\uD83D\uDD0D Processing callback for CheckoutRequestID: {}, ResultCode: {}, ResultDesc: {}", checkoutRequestId, resultCode, resultDesc);

            Optional<Transaction> optionalTransaction = transactionRepository.findByCheckoutRequestId(checkoutRequestId);

            if (optionalTransaction.isPresent()) {
                Transaction transaction = optionalTransaction.get();

                if (transaction.getStatus() == PaymentStatus.SUCCESS) {
                    log.warn("Transaction {} already marked as SUCCESS, skipping update", transaction.getId());
                    return ResponseEntity.ok("Transaction already processed");
                }

                transaction.setUpdatedAt(LocalDateTime.now());
                transaction.setDescription(resultDesc);

                if (resultCode == 0) {
                    transaction.setStatus(PaymentStatus.SUCCESS);

                    Map<String, Object> callbackMetadata = (Map<String, Object>) stkCallback.get("CallbackMetadata");
                    if (callbackMetadata != null) {
                        List<Map<String, Object>> items = (List<Map<String, Object>>) callbackMetadata.get("Item");
                        if (items != null) {
                            for (Map<String, Object> item : items) {
                                String name = (String) item.get("Name");
                                Object value = item.get("Value");

                                switch (name) {
                                    case "MpesaReceiptNumber" -> transaction.setMpesaReceiptNumber((String) value);
                                    case "TransactionDate" -> transaction.setTransactionTime(parseMpesaDate((Long) value));
                                }
                            }
                        }
                    }
                } else {
                    transaction.setStatus(PaymentStatus.FAILED);
                }

                transactionRepository.saveAndFlush(transaction);
                log.info("\u2705 Transaction updated in DB: {}", transaction);
            } else {
                log.warn("\u26A0\uFE0F No transaction found for CheckoutRequestID: {}", checkoutRequestId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Transaction not found");
            }

        } catch (IllegalArgumentException e) {
            log.error("\u274C Invalid callback data: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid callback data: " + e.getMessage());
        } catch (Exception e) {
            log.error("\u274C Error processing callback: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing callback");
        }

        return ResponseEntity.ok("Callback processed successfully");
    }

    private LocalDateTime parseMpesaDate(Long mpesaTimestamp) {
        String ts = mpesaTimestamp.toString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.parse(ts, formatter);
    }
    // ✅ Get all transaction history
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return ResponseEntity.ok(transactions);
    }

    // ✅ Get transaction history for a specific user by phoneNumber
    @GetMapping("/transactions/{phoneNumber}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable String phoneNumber) {
        List<Transaction> transactions = transactionRepository.findByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/transactions/id/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
}
