package com.ecommerce.auth.controller;

import com.ecommerce.auth.dto.ChangePasswordRequest;
import com.ecommerce.auth.dto.MessageResponse;
import com.ecommerce.auth.dto.UserProfileResponse;
import com.ecommerce.auth.security.UserDetailsImpl;
import com.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(authService.getUserProfile(userDetails.getId()));
    }

    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                          @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(userDetails.getId(), request));
    }
}
