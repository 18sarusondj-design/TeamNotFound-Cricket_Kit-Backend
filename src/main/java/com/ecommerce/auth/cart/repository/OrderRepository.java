package com.ecommerce.auth.cart.repository;

import com.ecommerce.auth.cart.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUser_Id(Long userId);
}
