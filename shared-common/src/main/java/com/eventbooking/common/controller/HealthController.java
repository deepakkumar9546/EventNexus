package com.eventbooking.common.controller;

import com.eventbooking.common.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("status", "UP");
        healthData.put("timestamp", LocalDateTime.now());
        healthData.put("service", getServiceName());
        
        return ApiResponse.success("Service is healthy", healthData);
    }
    
    private String getServiceName() {
        // This will be overridden by each service or determined from application properties
        return "Unknown Service";
    }
}