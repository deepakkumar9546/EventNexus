package com.eventbooking.event.service;

import com.eventbooking.event.dto.EventDto;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface CacheService {
    
    void cacheEvent(EventDto event);

    Optional<EventDto> getCachedEvent(UUID eventId);
    
    void evictEvent(UUID eventId);
    
    void cacheSearchResults(String cacheKey, Page<?> results);
    
    <T> Page<T> getCachedSearchResults(String cacheKey, Class<T> type);
    
    void evictSearchCaches(UUID categoryId, String city);
    
    String generateSearchCacheKey(Object criteria);
}