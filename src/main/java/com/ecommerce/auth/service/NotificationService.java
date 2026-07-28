package com.ecommerce.auth.service;

public interface NotificationService {
    void sendOtp(String identifier, String otp);
}
