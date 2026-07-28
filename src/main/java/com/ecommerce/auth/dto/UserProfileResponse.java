package com.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String fullName;
    private String email;
    private String mobileNumber;
    private LocalDateTime createdDate;
}
