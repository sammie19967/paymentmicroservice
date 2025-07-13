package com.academix.payment.model;

import com.academix.payment.model.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;

    private Double amount;

    private String mpesaReceiptNumber;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String merchantRequestId;

    private String checkoutRequestId;

    private LocalDateTime transactionTime;
}
