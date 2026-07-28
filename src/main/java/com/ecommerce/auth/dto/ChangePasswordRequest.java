package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_\\-]).{8,}$", 
             message = "Password must be at least 8 characters long, and include at least one uppercase letter, lowercase letter, numeric digit, and special character")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required")
    private String confirmPassword;
}
