package com.ecommerce.auth.controller;

import com.ecommerce.auth.cart.entity.Order;
import com.ecommerce.auth.cart.repository.OrderRepository;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.product.entity.Category;
import com.ecommerce.auth.product.entity.Product;
import com.ecommerce.auth.product.entity.ProductImage;
import com.ecommerce.auth.product.repository.CategoryRepository;
import com.ecommerce.auth.product.repository.ProductImageRepository;
import com.ecommerce.auth.product.repository.ProductRepository;
import com.ecommerce.auth.repository.UserRepository;
import com.ecommerce.auth.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    // ==========================================
    // PRODUCT MANAGEMENT
    // ==========================================

    @PostMapping("/products")
    public ResponseEntity<?> addProduct(@RequestBody Map<String, Object> payload) {
        String name = (String) payload.get("name");
        String desc = (String) payload.get("description");
        BigDecimal price = new BigDecimal(payload.get("price").toString());
        Integer stock = Integer.parseInt(payload.get("stock").toString());
        String catName = (String) payload.get("category");
        String imageUrl = (String) payload.get("imageUrl");

        Category category = categoryRepository.findAll().stream()
                .filter(c -> c.getCategoryName().equalsIgnoreCase(catName))
                .findFirst()
                .orElseGet(() -> categoryRepository.save(new Category(catName)));

        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        productRepository.save(p);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ProductImage pi = new ProductImage();
            pi.setImageUrl(imageUrl);
            pi.setProduct(p);
            productImageRepository.save(pi);
        }

        return ResponseEntity.ok(new MessageResponse("Product added successfully!"));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (payload.containsKey("name")) p.setName((String) payload.get("name"));
        if (payload.containsKey("description")) p.setDescription((String) payload.get("description"));
        if (payload.containsKey("price")) p.setPrice(new BigDecimal(payload.get("price").toString()));
        if (payload.containsKey("stock")) p.setStock(Integer.parseInt(payload.get("stock").toString()));
        
        if (payload.containsKey("category")) {
            String catName = (String) payload.get("category");
            Category category = categoryRepository.findAll().stream()
                    .filter(c -> c.getCategoryName().equalsIgnoreCase(catName))
                    .findFirst()
                    .orElseGet(() -> categoryRepository.save(new Category(catName)));
            p.setCategory(category);
        }

        productRepository.save(p);

        if (payload.containsKey("imageUrl")) {
            String imageUrl = (String) payload.get("imageUrl");
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Remove old images
                p.getImages().forEach(productImageRepository::delete);
                
                ProductImage pi = new ProductImage();
                pi.setImageUrl(imageUrl);
                pi.setProduct(p);
                productImageRepository.save(pi);
            }
        }

        return ResponseEntity.ok(new MessageResponse("Product updated successfully!"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(p);
        return ResponseEntity.ok(new MessageResponse("Product deleted successfully!"));
    }

    // ==========================================
    // USER MANAGEMENT
    // ==========================================

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (payload.containsKey("fullName")) u.setFullName((String) payload.get("fullName"));
        if (payload.containsKey("role")) u.setRole((String) payload.get("role"));
        if (payload.containsKey("password")) {
            String newPass = (String) payload.get("password");
            if (!newPass.isEmpty()) {
                u.setPasswordHash(passwordEncoder.encode(newPass));
            }
        }
        
        userRepository.save(u);
        return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
    }

    // ==========================================
    // ORDER & BUSINESS ANALYTICS
    // ==========================================

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderRepository.findAll());
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestBody Map<String, String> payload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (payload.containsKey("deliveryStatus")) {
            order.setDeliveryStatus(payload.get("deliveryStatus"));
            orderRepository.save(order);
        }
        return ResponseEntity.ok(new MessageResponse("Order status updated successfully!"));
    }

    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        Timestamp start = (startDate != null && !startDate.isEmpty()) 
                ? Timestamp.valueOf(LocalDateTime.parse(startDate, formatter))
                : Timestamp.valueOf(LocalDateTime.of(1970, 1, 1, 0, 0));
        
        Timestamp end = (endDate != null && !endDate.isEmpty()) 
                ? Timestamp.valueOf(LocalDateTime.parse(endDate, formatter))
                : Timestamp.valueOf(LocalDateTime.now());

        List<Order> successOrders = orderRepository.findByCreatedAtBetweenAndStatus(
                start, end, Order.OrderStatus.SUCCESS);

        BigDecimal totalRevenue = successOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("totalOrders", successOrders.size());
        result.put("totalRevenue", totalRevenue);
        return ResponseEntity.ok(result);
    }
}
