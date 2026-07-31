package com.ecommerce.auth.cart.service;

import com.ecommerce.auth.dto.OrderRequestDto;
import com.ecommerce.auth.dto.OrderResponseDto;
import com.ecommerce.auth.dto.PaymentVerificationDto;
import com.ecommerce.auth.cart.entity.Order;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.cart.repository.OrderRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    public OrderResponseDto createOrder(OrderRequestDto request, String email) throws RazorpayException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        int amountInPaise = (int) (request.getAmount() * 100);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
        String razorpayOrderId = razorpayOrder.get("id");

        Order order = new Order();
        order.setOrderId(razorpayOrderId);
        order.setUser(user);
        order.setTotalAmount(BigDecimal.valueOf(request.getAmount()));
        order.setStatus(Order.OrderStatus.PENDING);
        
        orderRepository.save(order);

        return OrderResponseDto.builder()
                .razorpayOrderId(razorpayOrderId)
                .amount(request.getAmount())
                .currency("INR")
                .build();
    }

    public boolean verifyPayment(PaymentVerificationDto verificationDto) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", verificationDto.getRazorpayOrderId());
            options.put("razorpay_payment_id", verificationDto.getRazorpayPaymentId());
            options.put("razorpay_signature", verificationDto.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);

            Order order = orderRepository.findById(verificationDto.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (isValid) {
                order.setStatus(Order.OrderStatus.SUCCESS);
                order.setRazorpayPaymentId(verificationDto.getRazorpayPaymentId());
                order.setRazorpaySignature(verificationDto.getRazorpaySignature());
                orderRepository.save(order);
                return true;
            } else {
                order.setStatus(Order.OrderStatus.FAILED);
                orderRepository.save(order);
                return false;
            }
        } catch (RazorpayException e) {
            e.printStackTrace();
            return false;
        }
    }
}
