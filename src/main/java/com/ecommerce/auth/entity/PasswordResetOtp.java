package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_otps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We can store the identifier (email or mobile) the OTP was sent to
    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false)
    private String otpHash; // Hashed OTP for security

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Column(nullable = false)
    private boolean verified = false;

    // Optional: a short-lived token generated after successful OTP verification
    // that allows the user to reset the password exactly once.
    @Column(unique = true)
    private String resetToken;
}
