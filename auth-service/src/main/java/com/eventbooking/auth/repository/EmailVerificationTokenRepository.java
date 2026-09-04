package com.eventbooking.auth.repository;

import com.eventbooking.auth.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    
    /**
     * Find valid token by token string
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.token = :token AND t.used = false AND t.expiresAt > :now")
    Optional<EmailVerificationToken> findValidTokenByToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Find token by token string (regardless of validity)
     */
    Optional<EmailVerificationToken> findByToken(String token);
    
    /**
     * Find all tokens for a user
     */
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Optional<EmailVerificationToken> findByUserId(@Param("userId") UUID userId);
    
    /**
     * Invalidate all tokens for a user
     */
    @Modifying
    @Query("UPDATE EmailVerificationToken t SET t.used = true, t.usedAt = :now WHERE t.userId = :userId AND t.used = false")
    void invalidateAllUserTokens(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :expirationTime")
    void deleteExpiredTokens(@Param("expirationTime") LocalDateTime expirationTime);
    
    /**
     * Count recent verification requests by user
     */
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.userId = :userId AND t.createdAt > :since")
    long countRecentRequestsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
