package com.eventbooking.auth.repository;

import com.eventbooking.auth.entity.PasswordResetToken;
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
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    
    /**
     * Find password reset token by token string
     */
    Optional<PasswordResetToken> findByToken(String token);
    
    /**
     * Find valid (unused and not expired) token by token string
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.token = :token AND t.isUsed = false AND t.expiresAt > :now")
    Optional<PasswordResetToken> findValidTokenByToken(@Param("token") String token, @Param("now") LocalDateTime now);
    
    /**
     * Find all tokens for a user
     */
    List<PasswordResetToken> findByUserId(UUID userId);
    
    /**
     * Find valid tokens for a user
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.userId = :userId AND t.isUsed = false AND t.expiresAt > :now")
    List<PasswordResetToken> findValidTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    
    /**
     * Find expired tokens
     */
    @Query("SELECT t FROM PasswordResetToken t WHERE t.expiresAt <= :now")
    List<PasswordResetToken> findExpiredTokens(@Param("now") LocalDateTime now);
    
    /**
     * Count recent password reset requests for a user (for rate limiting)
     */
    @Query("SELECT COUNT(t) FROM PasswordResetToken t WHERE t.userId = :userId AND t.createdAt >= :since")
    long countRecentRequestsByUserId(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
    
    /**
     * Mark token as used
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.token = :token")
    void markTokenAsUsed(@Param("token") String token);
    
    /**
     * Invalidate all tokens for a user
     */
    @Modifying
    @Query("UPDATE PasswordResetToken t SET t.isUsed = true WHERE t.userId = :userId AND t.isUsed = false")
    void invalidateAllUserTokens(@Param("userId") UUID userId);
    
    /**
     * Delete expired tokens
     */
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.expiresAt <= :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}