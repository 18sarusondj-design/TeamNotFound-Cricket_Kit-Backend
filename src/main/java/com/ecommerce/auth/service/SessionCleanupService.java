package com.ecommerce.auth.service;

import com.ecommerce.auth.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupService.class);
    private final UserSessionRepository userSessionRepository;

    // Run every day at midnight (00:00:00)
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void cleanupExpiredSessions() {
        logger.info("Starting scheduled cleanup of expired user sessions...");
        try {
            userSessionRepository.deleteExpiredSessions(LocalDateTime.now());
            logger.info("Successfully cleaned up expired user sessions.");
        } catch (Exception e) {
            logger.error("Failed to clean up expired sessions: {}", e.getMessage());
        }
    }
}
