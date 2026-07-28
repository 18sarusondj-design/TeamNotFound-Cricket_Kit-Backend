package com.ecommerce.auth.cart.controller;

import com.ecommerce.auth.cart.entity.CartItem;
import com.ecommerce.auth.cart.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private com.ecommerce.auth.repository.UserRepository userRepository;

    @Autowired
    private com.ecommerce.auth.product.repository.ProductRepository productRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartItemRepository.findByUser_Id(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody com.ecommerce.auth.dto.AddToCartRequest request) {
        com.ecommerce.auth.entity.User user = userRepository.findById(request.getUserId()).orElse(null);
        com.ecommerce.auth.product.entity.Product product = productRepository.findById(request.getProductId()).orElse(null);
        
        if (user == null || product == null) {
            return ResponseEntity.badRequest().body(new com.ecommerce.auth.dto.MessageResponse("User or Product not found"));
        }

        CartItem item = cartItemRepository.findByUser_IdAndProduct_ProductId(request.getUserId(), request.getProductId()).orElse(new CartItem());
        
        if (item.getId() == null) {
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
        } else {
            item.setQuantity(item.getQuantity() + request.getQuantity());
        }
        
        cartItemRepository.save(item);
        return ResponseEntity.ok(new com.ecommerce.auth.dto.MessageResponse("Added to cart successfully"));
    }
}
