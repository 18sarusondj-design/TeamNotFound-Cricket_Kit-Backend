package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    @Async
    @Override
    public void sendOtp(String identifier, String otp) {
        if (identifier.contains("@")) {
            logger.info("=========================================================");
            logger.info("LOCAL DEV MODE: Email OTP for {} is: {}", identifier, otp);
            logger.info("=========================================================");
            try {
                if (brevoApiKey == null || brevoApiKey.isEmpty()) {
                    logger.error("BREVO_API_KEY is not set! Skipping email sending.");
                    return;
                }

                String url = "https://api.brevo.com/v3/smtp/email";
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", brevoApiKey);
                
                Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", "TeamNotFound", "email", senderEmail),
                    "to", List.of(Map.of("email", identifier)),
                    "subject", "Your Registration OTP - TeamNotFound",
                    "htmlContent", "<p>Your One-Time Password (OTP) is: <strong>" + otp + "</strong></p><p>This OTP is valid for 10 minutes. Do not share this code with anyone.</p>"
                );

                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                
                logger.info("Email OTP sent successfully to {} via Brevo! Response: {}", identifier, response.getStatusCode());
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
