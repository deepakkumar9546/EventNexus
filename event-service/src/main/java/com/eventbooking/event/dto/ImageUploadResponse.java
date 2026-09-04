package com.eventbooking.event.dto;

public class ImageUploadResponse {
    
    private String imageUrl;
    private String cdnUrl;
    private String fileName;
    private Long fileSize;
    
    public ImageUploadResponse() {}
    
    public ImageUploadResponse(String imageUrl, String cdnUrl, String fileName, Long fileSize) {
        this.imageUrl = imageUrl;
        this.cdnUrl = cdnUrl;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getCdnUrl() {
        return cdnUrl;
    }
    
    public void setCdnUrl(String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
}
