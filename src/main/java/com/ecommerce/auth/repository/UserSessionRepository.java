package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByJwtToken(String jwtToken);

    @Modifying
    @Transactional
    @Query("UPDATE UserSession s SET s.active = false WHERE s.user.id = :userId")
    void revokeAllUserSessions(Long userId);
}
