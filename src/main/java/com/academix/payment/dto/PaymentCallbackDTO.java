package com.academix.payment.dto;

import lombok.Data;

@Data
public class PaymentCallbackDTO {
    private String merchantRequestID;
    private String checkoutRequestID;
    private int resultCode;
    private String resultDesc;
    private String phoneNumber;
    private double amount;
    private String mpesaReceiptNumber;
    private String transactionDate;
}
