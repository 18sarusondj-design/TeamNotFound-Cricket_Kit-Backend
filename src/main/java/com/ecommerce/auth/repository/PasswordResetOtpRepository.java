package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByIdentifier(String identifier);
    
    Optional<PasswordResetOtp> findByResetToken(String resetToken);
}
