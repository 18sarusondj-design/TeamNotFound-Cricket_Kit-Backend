package com.ecommerce.auth.cart.repository;

import com.ecommerce.auth.cart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.sql.Timestamp;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_Id(Long userId);
    
    List<Order> findByCreatedAtBetweenAndStatus(Timestamp start, Timestamp end, Order.OrderStatus status);
}
