package com.ecommerce.auth.cart.repository;

import com.ecommerce.auth.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser_Id(Long userId);
    java.util.Optional<CartItem> findByUser_IdAndProduct_ProductId(Long userId, Long productId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser_Id(Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByUser_IdAndProduct_ProductId(Long userId, Long productId);
}
