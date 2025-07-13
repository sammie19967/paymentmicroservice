package com.academix.payment.model;

import com.academix.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String reference;
    private String merchantRequestId;
    private String checkoutRequestId;
    private String mpesaReceiptNumber;
    private LocalDateTime transactionTime;

    private String description; // ✅ Added field for description

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // ✅ Added field for updated timestamp
}
