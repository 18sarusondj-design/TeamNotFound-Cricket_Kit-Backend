package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.sender.email:kumarswamyhiremath22@gmail.com}")
    private String senderEmail;

    @Async
    @Override
    public void sendOtp(String identifier, String otp) {
        if (identifier.contains("@")) {
            logger.info("=========================================================");
            logger.info("LOCAL DEV MODE: Email OTP for {} is: {}", identifier, otp);
            logger.info("=========================================================");
            try {
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", apiKey);
                headers.setAccept(List.of(MediaType.APPLICATION_JSON));

                Map<String, Object> body = Map.of(
                    "sender", Map.of("name", "Team Not Found", "email", senderEmail),
                    "to", List.of(Map.of("email", identifier)),
                    "subject", "Your Registration OTP",
                    "textContent", "Your One-Time Password (OTP) is: " + otp + "\n\nThis OTP is valid for 10 minutes. Do not share this code with anyone."
                );

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", entity, String.class);
                
                logger.info("Email OTP sent successfully to {} via Brevo HTTP API! Status: {}", identifier, response.getStatusCode());
            } catch (Exception e) {
                logger.error("Failed to send email OTP to {} via Brevo: {}", identifier, e.getMessage());
            }
        } else {
            // For mobile numbers, since we only have email setup, just mock it.
            logger.info("=========================================================");
            logger.info("MOCK SMS NOTIFICATION: Sending OTP to {}", identifier);
            logger.info("OTP: {}", otp);
            logger.info("=========================================================");
        }
    }
}
