package com.academix.payment;

import com.academix.payment.dto.PaymentRequest;
import com.academix.payment.service.MpesaPaymentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean
    CommandLineRunner run(MpesaPaymentService paymentService) {
        return args -> {
            PaymentRequest request = new PaymentRequest();
            request.setPhoneNumber("254725153581"); // Replace with a valid Safaricom number
            request.setAmount(1); // Test amount in KES
            request.setDescription("Test STK Push");

            try {
                paymentService.initiatePayment(request);

                System.out.println("✅ STK Push initiated successfully:");
                System.out.println("Phone Number: " + request.getPhoneNumber());
                System.out.println("Amount: " + request.getAmount() + " KES");
            } catch (Exception e) {
                System.err.println("❌ Failed to initiate STK Push: " + e.getMessage());
            }
        };
    }
}
