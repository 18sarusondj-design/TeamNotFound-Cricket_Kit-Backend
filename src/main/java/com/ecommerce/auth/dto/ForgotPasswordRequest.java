package com.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email or mobile number is required")
    private String identifier;
}
