package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    private String identifier; // Can be email or mobile number

    @NotBlank(message = "Password is required")
    private String password;
}
