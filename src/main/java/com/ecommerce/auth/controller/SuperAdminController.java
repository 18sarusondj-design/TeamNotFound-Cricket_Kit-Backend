package com.ecommerce.auth.controller;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
public class SuperAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> payload) {
        if (userRepository.existsByEmail((String) payload.get("email"))) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email already in use."));
        }

        User u = User.builder()
                .fullName((String) payload.get("fullName"))
                .email((String) payload.get("email"))
                .mobileNumber((String) payload.get("mobileNumber"))
                .passwordHash(passwordEncoder.encode((String) payload.get("password")))
                .role((String) payload.get("role"))
                .isVerified(true)
                .build();
        
        userRepository.save(u);
        return ResponseEntity.ok(new MessageResponse("User created successfully!"));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (payload.containsKey("fullName")) u.setFullName((String) payload.get("fullName"));
        if (payload.containsKey("role")) u.setRole((String) payload.get("role"));
        if (payload.containsKey("password")) {
            String newPass = (String) payload.get("password");
            if (newPass != null && !newPass.isEmpty()) {
                u.setPasswordHash(passwordEncoder.encode(newPass));
            }
        }
        
        userRepository.save(u);
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(u);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
    }

    @PutMapping("/profile/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));

        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("Password cannot be empty"));
        }

        u.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(u);
        return ResponseEntity.ok(new MessageResponse("Password updated successfully!"));
    }
}
