package com.eventbooking.event.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.eventbooking.event.exception.InvalidEventDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);
    
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    
    private final AmazonS3 amazonS3;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.cloudfront.domain:}")
    private String cloudFrontDomain;
    
    @Autowired
    public ImageServiceImpl(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }
    
    @Override
    public String uploadEventImage(MultipartFile file, UUID eventId) throws IOException {
        // Validate file
        validateImage(file);
        
        // Generate unique file name
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = String.format("events/%s/%s.%s", 
            eventId, UUID.randomUUID(), extension);
        
        // Resize and optimize image
        byte[] optimizedImage = resizeAndOptimizeImage(file);
        
        // Upload to S3
        try (InputStream inputStream = new ByteArrayInputStream(optimizedImage)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(optimizedImage.length);
            metadata.setContentType(file.getContentType());
            metadata.setCacheControl("public, max-age=31536000"); // 1 year cache
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                bucketName, fileName, inputStream, metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead);
            
            amazonS3.putObject(putObjectRequest);
            
            String s3Url = amazonS3.getUrl(bucketName, fileName).toString();
            logger.info("Successfully uploaded image to S3: {}", s3Url);
            
            return s3Url;
        } catch (Exception e) {
            logger.error("Failed to upload image to S3", e);
            throw new IOException("Failed to upload image", e);
        }
    }
    
    @Override
    public void deleteEventImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract key from S3 URL
            String key = extractKeyFromUrl(imageUrl);
            
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, key);
            amazonS3.deleteObject(deleteObjectRequest);
            
            logger.info("Successfully deleted image from S3: {}", key);
        } catch (Exception e) {
            logger.error("Failed to delete image from S3: {}", imageUrl, e);
            // Don't throw exception, just log the error
        }
    }
    
    @Override
    public String getCdnUrl(String s3Url) {
        if (cloudFrontDomain == null || cloudFrontDomain.isEmpty()) {
            return s3Url;
        }
        
        String key = extractKeyFromUrl(s3Url);
        return String.format("https://%s/%s", cloudFrontDomain, key);
    }
    
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidEventDataException("Image file is required");
        }
        
        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidEventDataException(
                String.format("Image file size exceeds maximum allowed size of %d MB", 
                    MAX_FILE_SIZE / (1024 * 1024)));
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidEventDataException(
                "Invalid image format. Allowed formats: JPEG, PNG, WebP");
        }
        
        // Validate it's actually an image
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new InvalidEventDataException("File is not a valid image");
            }
        } catch (IOException e) {
            throw new InvalidEventDataException("Failed to read image file");
        }
    }
    
    private byte[] resizeAndOptimizeImage(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Failed to read image");
        }
        
        // Calculate new dimensions while maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        int newWidth = originalWidth;
        int newHeight = originalHeight;
        
        if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
            double widthRatio = (double) MAX_WIDTH / originalWidth;
            double heightRatio = (double) MAX_HEIGHT / originalHeight;
            double ratio = Math.min(widthRatio, heightRatio);
            
            newWidth = (int) (originalWidth * ratio);
            newHeight = (int) (originalHeight * ratio);
        }
        
        // Resize image if needed
        BufferedImage resizedImage;
        if (newWidth != originalWidth || newHeight != originalHeight) {
            resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = resizedImage.createGraphics();
            
            // Use high-quality rendering
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            
            graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            graphics.dispose();
        } else {
            resizedImage = originalImage;
        }
        
        // Convert to byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String formatName = getImageFormat(file.getContentType());
        ImageIO.write(resizedImage, formatName, outputStream);
        
        return outputStream.toByteArray();
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    private String getImageFormat(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        
        switch (contentType.toLowerCase()) {
            case "image/png":
                return "png";
            case "image/webp":
                return "webp";
            case "image/jpeg":
            case "image/jpg":
            default:
                return "jpg";
        }
    }
    
    private String extractKeyFromUrl(String url) {
        // Extract key from S3 URL
        // Format: https://bucket-name.s3.region.amazonaws.com/key
        // or: https://s3.region.amazonaws.com/bucket-name/key
        
        if (url.contains(bucketName)) {
            int bucketIndex = url.indexOf(bucketName);
            int keyStartIndex = url.indexOf("/", bucketIndex + bucketName.length());
            if (keyStartIndex != -1) {
                return url.substring(keyStartIndex + 1);
            }
        }
        
        // Fallback: assume everything after the domain is the key
        try {
            java.net.URL parsedUrl = new java.net.URL(url);
            String path = parsedUrl.getPath();
            return path.startsWith("/") ? path.substring(1) : path;
        } catch (Exception e) {
            logger.error("Failed to extract key from URL: {}", url, e);
            return url;
        }
    }
}
