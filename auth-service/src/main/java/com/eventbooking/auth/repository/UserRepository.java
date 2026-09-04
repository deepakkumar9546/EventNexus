package com.eventbooking.auth.repository;

import com.eventbooking.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Find user by email address
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find users by email verification status
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = :verified")
    Optional<User> findByEmailVerified(@Param("verified") boolean verified);
    
    /**
     * Find users with failed login attempts greater than specified count
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    Optional<User> findByFailedLoginAttemptsGreaterThanEqual(@Param("attempts") int attempts);
    
    /**
     * Find locked accounts that should be unlocked (after 30 minutes)
     */
    @Query("SELECT u FROM User u WHERE u.accountLocked = true AND u.lastLoginAttempt < :unlockTime")
    Optional<User> findLockedAccountsToUnlock(@Param("unlockTime") LocalDateTime unlockTime);
    
    /**
     * Reset failed login attempts for a user
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0, u.accountLocked = false WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") UUID userId);
    
    /**
     * Update email verification status
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") UUID userId, @Param("verified") boolean verified);
    
    /**
     * Update password hash
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.id = :userId")
    void updatePasswordHash(@Param("userId") UUID userId, @Param("passwordHash") String passwordHash);
}