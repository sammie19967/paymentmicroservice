package com.academix.payment;

import com.academix.payment.model.TestEntity;
import com.academix.payment.repository.TestEntityRepository;
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
    CommandLineRunner run(TestEntityRepository repository) {
        return args -> {
            TestEntity entity = new TestEntity();
            entity.setMessage("Hello DB");
            repository.save(entity);
            System.out.println("✅ Saved test record to PostgreSQL.");
        };
    }
}
