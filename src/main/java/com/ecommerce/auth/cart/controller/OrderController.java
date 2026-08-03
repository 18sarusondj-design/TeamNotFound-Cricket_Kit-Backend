package com.ecommerce.auth.cart.controller;

import com.ecommerce.auth.dto.OrderRequestDto;
import com.ecommerce.auth.dto.OrderResponseDto;
import com.ecommerce.auth.dto.PaymentVerificationDto;
import com.ecommerce.auth.cart.service.OrderService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import com.ecommerce.auth.cart.entity.Order;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequestDto request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            OrderResponseDto response = orderService.createOrder(request, email);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationDto verificationDto) {
        boolean isSuccess = orderService.verifyPayment(verificationDto);
        if (isSuccess) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified and order placed."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "failed", "error", "Payment verification failed."));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserOrders() {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Order> orders = orderService.getUserOrders(email);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
