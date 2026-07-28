package com.ecommerce.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pending_users")
public class PendingUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String mobileNumber;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String otpHash;
    
    @Column(nullable = false)
    private LocalDateTime expiryTime;
}
