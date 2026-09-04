package com.eventbooking.auth.repository;

import com.eventbooking.auth.model.RedisUserSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RedisUserSessionRepository extends CrudRepository<RedisUserSession, String> {
    
    /**
     * Find session by user ID
     */
    List<RedisUserSession> findByUserId(UUID userId);
    
    /**
     * Find session by token hash
     */
    Optional<RedisUserSession> findByTokenHash(String tokenHash);
    
    /**
     * Delete all sessions for a user
     */
    void deleteByUserId(UUID userId);
}