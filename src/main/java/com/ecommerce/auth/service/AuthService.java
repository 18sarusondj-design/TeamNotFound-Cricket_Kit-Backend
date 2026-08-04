package com.ecommerce.auth.service;

import com.ecommerce.auth.dto.*;
import com.ecommerce.auth.entity.PasswordResetOtp;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserSession;
import com.ecommerce.auth.entity.PendingUser;
import com.ecommerce.auth.exception.BadRequestException;
import com.ecommerce.auth.exception.InvalidCredentialsException;
import com.ecommerce.auth.exception.ResourceNotFoundException;
import com.ecommerce.auth.exception.UnverifiedUserException;
import com.ecommerce.auth.repository.PasswordResetOtpRepository;
import com.ecommerce.auth.repository.PendingUserRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.repository.UserSessionRepository;
import com.ecommerce.auth.security.JwtUtil;
import com.ecommerce.auth.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PendingUserRepository pendingUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final NotificationService notificationService;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    @Value("${otp.expiration}")
    private int otpExpirationMs;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }
        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException("Mobile number is already in use");
        }

        if (pendingUserRepository.existsByEmail(request.getEmail())) {
            pendingUserRepository.findByEmail(request.getEmail()).ifPresent(user -> {
                LocalDateTime canResendAt = user.getExpiryTime().minusSeconds((otpExpirationMs / 1000) - 60);
                if (LocalDateTime.now().isBefore(canResendAt)) {
                    throw new BadRequestException("Please wait 1 minute before requesting a new OTP.");
                }
                pendingUserRepository.delete(user);
                pendingUserRepository.flush();
            });
        }

        String otpCode = generateOtp();

        PendingUser pendingUser = PendingUser.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .otpHash(passwordEncoder.encode(otpCode))
                .expiryTime(LocalDateTime.now().plusSeconds(otpExpirationMs / 1000))
                .build();

        pendingUserRepository.save(pendingUser);
        notificationService.sendOtp(request.getEmail(), otpCode);

        return new MessageResponse("User registered temporarily. Please verify your email.");
    }

    @Transactional(noRollbackFor = UnverifiedUserException.class)
    public JwtResponse login(LoginRequest request) {
        // Check if user actually exists before attempting to authenticate
        Optional<User> userOpt = userRepository.findByEmail(request.getIdentifier());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByMobileNumber(request.getIdentifier());
        }
        
        if (userOpt.isEmpty()) {
            Optional<PendingUser> pendingOpt = pendingUserRepository.findByEmail(request.getIdentifier());
            if (pendingOpt.isPresent()) {
                PendingUser pendingUser = pendingOpt.get();
                if (passwordEncoder.matches(request.getPassword(), pendingUser.getPasswordHash())) {
                    String otpCode = generateOtp();
                    pendingUser.setOtpHash(passwordEncoder.encode(otpCode));
                    pendingUser.setExpiryTime(LocalDateTime.now().plusSeconds(otpExpirationMs / 1000));
                    pendingUserRepository.save(pendingUser);
                    notificationService.sendOtp(pendingUser.getEmail(), otpCode);
                    throw new UnverifiedUserException("User not verified. A new OTP has been sent.", pendingUser.getEmail());
                } else {
                    throw new InvalidCredentialsException("Incorrect password. Please try again.");
                }
            }
            throw new BadRequestException("User does not exist. Please go sign up!");
        }
        
        // Authenticate with Spring Security
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword()));
        } catch (BadCredentialsException e) {
            // Because we verified the user exists above, if this fails it means the password is wrong
            throw new InvalidCredentialsException("Incorrect password. Please try again.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isVerified()) {
            throw new BadRequestException("Please verify your email address before logging in.");
        }

        // Store session
        UserSession session = UserSession.builder()
                .user(user)
                .jwtToken(jwt)
                .loginTime(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusSeconds(jwtExpirationMs / 1000))
                .active(true)
                .build();
        userSessionRepository.save(session);

        return new JwtResponse(jwt, "Bearer", userDetails.getId(), userDetails.getEmail(), user.getFullName(), user.getRole());
    }

    @Transactional
    public MessageResponse logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        Optional<UserSession> sessionOpt = userSessionRepository.findByJwtToken(token);
        if (sessionOpt.isPresent()) {
            userSessionRepository.delete(sessionOpt.get());
        }
        
        SecurityContextHolder.clearContext();
        return new MessageResponse("Logged out successfully");
    }

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        // Do not reveal if user exists or not, always return same generic response
        Optional<User> userOpt = userRepository.findByEmail(request.getIdentifier());
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByMobileNumber(request.getIdentifier());
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Invalidate any existing OTPs for this identifier
            Optional<PasswordResetOtp> existingOtpOpt = otpRepository.findByIdentifier(request.getIdentifier());
            if (existingOtpOpt.isPresent()) {
                PasswordResetOtp existingOtp = existingOtpOpt.get();
                LocalDateTime canResendAt = existingOtp.getExpiryTime().minusSeconds((otpExpirationMs / 1000) - 60);
                if (LocalDateTime.now().isBefore(canResendAt)) {
                    throw new BadRequestException("Please wait 1 minute before requesting a new password reset OTP.");
                }
                otpRepository.delete(existingOtp);
            }

            String otpCode = generateOtp();
            
            PasswordResetOtp otpEntity = PasswordResetOtp.builder()
                    .identifier(request.getIdentifier())
                    .otpHash(passwordEncoder.encode(otpCode))
                    .expiryTime(LocalDateTime.now().plusSeconds(otpExpirationMs / 1000))
                    .verified(false)
                    .build();
                    
            otpRepository.save(otpEntity);
            notificationService.sendOtp(request.getIdentifier(), otpCode);
        }
        
        return new MessageResponse("If your identifier is registered, an OTP will be sent.");
    }

    @Transactional
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        PasswordResetOtp otpEntity = otpRepository.findByIdentifier(request.getIdentifier())
                .orElseThrow(() -> new BadRequestException("Invalid or expired OTP"));
                
        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpEntity);
            throw new BadRequestException("Invalid or expired OTP");
        }
        
        if (!passwordEncoder.matches(request.getOtp(), otpEntity.getOtpHash())) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        
        otpEntity.setVerified(true);
        String resetToken = UUID.randomUUID().toString();
        otpEntity.setResetToken(resetToken);
        otpRepository.save(otpEntity);
        
        return new MessageResponse(resetToken); // In real app, return this securely or use a session
    }

    @Transactional
    public MessageResponse verifyRegistrationOtp(VerifyRegistrationRequest request) {
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Registration request not found or expired"));
                
        if (pendingUser.getExpiryTime().isBefore(LocalDateTime.now())) {
            pendingUserRepository.delete(pendingUser);
            throw new BadRequestException("OTP expired. Please register again.");
        }
        
        if (!passwordEncoder.matches(request.getOtp(), pendingUser.getOtpHash())) {
            throw new BadRequestException("Invalid OTP");
        }
        
        User user = User.builder()
                .fullName(pendingUser.getFullName())
                .email(pendingUser.getEmail())
                .mobileNumber(pendingUser.getMobileNumber())
                .passwordHash(pendingUser.getPasswordHash())
                .isVerified(true)
                .build();
                
        userRepository.save(user);
        pendingUserRepository.delete(pendingUser);
        
        return new MessageResponse("Email verified successfully");
    }

    @Transactional
    public MessageResponse resendRegistrationOtp(ResendOtpRequest request) {
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Registration request not found. Please register first."));
                
        LocalDateTime canResendAt = pendingUser.getExpiryTime().minusSeconds((otpExpirationMs / 1000) - 60);
        if (LocalDateTime.now().isBefore(canResendAt)) {
            throw new BadRequestException("Please wait 1 minute before requesting a new OTP.");
        }
                
        String otpCode = generateOtp();
        pendingUser.setOtpHash(passwordEncoder.encode(otpCode));
        pendingUser.setExpiryTime(LocalDateTime.now().plusSeconds(otpExpirationMs / 1000));
        
        pendingUserRepository.save(pendingUser);
        notificationService.sendOtp(request.getEmail(), otpCode);

        return new MessageResponse("A new OTP has been sent to your email.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        PasswordResetOtp otpEntity = otpRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
                
        if (!otpEntity.isVerified()) {
            throw new BadRequestException("OTP was not verified");
        }
        
        User user = userRepository.findByEmail(otpEntity.getIdentifier())
                .orElseGet(() -> userRepository.findByMobileNumber(otpEntity.getIdentifier())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));
                        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Invalidate sessions
        userSessionRepository.revokeAllUserSessions(user.getId());
        
        // Delete used OTP
        otpRepository.delete(otpEntity);
        
        return new MessageResponse("Password reset successfully");
    }

    @Transactional
    public MessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid current password");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Optionally invalidate other sessions, keeping current one active. 
        // For simplicity, let's invalidate all sessions, forcing re-login.
        userSessionRepository.revokeAllUserSessions(user.getId());
        
        return new MessageResponse("Password changed successfully. Please log in again.");
    }
    
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .createdDate(user.getCreatedDate())
                .build();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
