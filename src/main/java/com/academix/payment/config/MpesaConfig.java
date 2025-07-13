package com.academix.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Data
public class MpesaConfig {
    private String consumerKey;
    private String consumerSecret;
    private String passkey;
    private String businessShortCode;
    private String callbackUrl;
    private String accessTokenUrl;
    private String stkPushUrl;
}