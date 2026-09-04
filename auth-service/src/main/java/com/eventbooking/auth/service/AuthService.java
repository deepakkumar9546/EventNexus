package com.eventbooking.auth.service;

import com.eventbooking.auth.dto.*;
import com.eventbooking.common.dto.UserDto;

public interface AuthService {
    
    /**
     * Register a new user
     */
    UserDto registerUser(RegisterRequest request, String clientInfo);
    
    /**
     * Verify user email with token
     */
    void verifyEmail(String token);
    
    /**
     * Authenticate user and return login response
     */
    LoginResponse authenticateUser(LoginRequest request, String clientInfo);
    
    /**
     * Logout user by invalidating session
     */
    void logoutUser(String token);
    
    /**
     * Initiate password reset process
     */
    void initiatePasswordReset(String email, String clientInfo);
    
    /**
     * Reset password with token
     */
    void resetPassword(ResetPasswordRequest request);
    
    /**
     * Get user profile from token
     */
    UserDto getUserProfile(String token);
    
    /**
     * Validate token and return user information
     */
    UserDto validateTokenAndGetUser(String token);
    
    /**
     * Resend email verification
     */
    void resendEmailVerification(String email);
}