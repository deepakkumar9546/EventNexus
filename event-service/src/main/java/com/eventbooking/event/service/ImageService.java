package com.eventbooking.event.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ImageService {
    
    /**
     * Upload an image to S3 with validation and optimization
     * 
     * @param file The image file to upload
     * @param eventId The event ID for organizing images
     * @return The S3 URL of the uploaded image
     * @throws IOException if upload fails
     */
    String uploadEventImage(MultipartFile file, UUID eventId) throws IOException;
    
    /**
     * Delete an image from S3
     * 
     * @param imageUrl The S3 URL of the image to delete
     */
    void deleteEventImage(String imageUrl);
    
    /**
     * Get CloudFront CDN URL for an S3 image
     * 
     * @param s3Url The S3 URL
     * @return The CloudFront CDN URL
     */
    String getCdnUrl(String s3Url);
}
