package com.eventbooking.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@RedisHash("user_session")
public class RedisUserSession implements Serializable {
    
    @Id
    private String sessionId;
    
    @Indexed
    private UUID userId;
    
    private String email;
    private String firstName;
    private String lastName;
    private boolean emailVerified;
    
    @Indexed
    private String tokenHash;
    
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivity;
    
    @TimeToLive
    private Long ttl; // Time to live in seconds

    // Default constructor
    public RedisUserSession() {}

    // Constructor
    public RedisUserSession(String sessionId, UUID userId, String email, String firstName, 
                           String lastName, boolean emailVerified, String tokenHash, 
                           String deviceInfo, String ipAddress, Long ttl) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailVerified = emailVerified;
        this.tokenHash = tokenHash;
        this.deviceInfo = deviceInfo;
        this.ipAddress = ipAddress;
        this.loginTime = LocalDateTime.now();
        this.lastActivity = LocalDateTime.now();
        this.ttl = ttl;
    }

    // Getters and setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public LocalDateTime getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}