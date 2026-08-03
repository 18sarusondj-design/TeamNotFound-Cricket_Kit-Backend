package com.ecommerce.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    
    @Autowired
    private JavaMailSender mailSender;

    @Async
    @Override
    public void sendOtp(String identifier, String otp) {
        if (identifier.contains("@")) {
            logger.info("=========================================================");
            logger.info("LOCAL DEV MODE: Email OTP for {} is: {}", identifier, otp);
            logger.info("=========================================================");
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(identifier);
                message.setSubject("Your Registration OTP - TeamNotFound");
                message.setText("Your One-Time Password (OTP) is: " + otp + "\n\nThis OTP is valid for 10 minutes. Do not share this code with anyone.");
                
                mailSender.send(message);
                logger.info("Email OTP sent successfully to {} via Gmail SMTP!", identifier);
            } catch (Exception e) {
                logger.error("Failed to send email OTP to {} via Gmail: {}", identifier, e.getMessage());
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
