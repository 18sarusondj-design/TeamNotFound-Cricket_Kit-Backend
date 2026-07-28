package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {
    Optional<PendingUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
