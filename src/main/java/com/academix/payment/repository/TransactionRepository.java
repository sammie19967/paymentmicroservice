package com.academix.payment.repository;

import com.academix.payment.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByReference(String reference);

    // ✅ Add this missing method
    Optional<Transaction> findByCheckoutRequestId(String checkoutRequestId);
}
