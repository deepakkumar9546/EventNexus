package com.eventbooking.auth.repository;

import com.eventbooking.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    
    /**
     * Find session by token hash
     */
    Optional<UserSession> findByTokenHash(String tokenHash);
    
    /**
     * Find active sessions for a user
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Find all sessions for a user
     */
    List<UserSession> findByUserId(UUID userId);
    
    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM UserSession s WHERE s.expiresAt <= :now")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Deactivate all sessions for a user
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.userId = :userId")
    void deactivateAllUserSessions(@Param("userId") UUID userId);
    
    /**
     * Deactivate session by token hash
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.tokenHash = :tokenHash")
    void deactivateSessionByTokenHash(@Param("tokenHash") String tokenHash);
    
    /**
     * Delete expired sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt <= :now")
    void deleteExpiredSessions(@Param("now") LocalDateTime now);
    
    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.isActive = true AND s.expiresAt > :now")
    long countActiveSessionsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}