package com.academix.payment.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private String phoneNumber;
    private double amount;
    private String description;
}
