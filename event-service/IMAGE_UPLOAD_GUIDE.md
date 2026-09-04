# Event Image Upload Guide

## Overview

The Event Management Service supports uploading, managing, and delivering event images through AWS S3 and CloudFront CDN.

## Features

- **Image Validation**: Supports JPEG, PNG, and WebP formats up to 10MB
- **Automatic Resizing**: Images are resized to max 1920x1080 while maintaining aspect ratio
- **Optimization**: Images are optimized for web delivery
- **S3 Storage**: Secure storage in AWS S3 with public read access
- **CDN Delivery**: Optional CloudFront CDN integration for fast global delivery
- **Image Management**: Upload, update, and delete event images

## Configuration

### Required Environment Variables

```bash
# AWS Credentials
AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
AWS_REGION=us-east-1

# S3 Bucket
AWS_S3_BUCKET=event-images-bucket

# CloudFront (Optional)
AWS_CLOUDFRONT_DOMAIN=d1234567890.cloudfront.net
```

### AWS S3 Bucket Setup

1. Create an S3 bucket for event images
2. Configure bucket policy for public read access:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::event-images-bucket/*"
    }
  ]
}
```

3. Enable CORS if needed:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": []
  }
]
```

### CloudFront CDN Setup (Optional)

1. Create a CloudFront distribution
2. Set origin to your S3 bucket
3. Configure caching behavior (recommended: 1 year cache)
4. Add the CloudFront domain to `AWS_CLOUDFRONT_DOMAIN` environment variable

## API Endpoints

### Upload Event Image

**Endpoint**: `POST /api/events/{eventId}/image`

**Headers**:
- `Authorization: Bearer {jwt-token}`
- `Content-Type: multipart/form-data`

**Request**:
```
Form Data:
- image: [image file]
```

**Response**:
```json
{
  "success": true,
  "data": {
    "imageUrl": "https://event-images-bucket.s3.us-east-1.amazonaws.com/events/uuid/image.jpg",
    "cdnUrl": "https://d1234567890.cloudfront.net/events/uuid/image.jpg",
    "fileName": "original-filename.jpg",
    "fileSize": 1024000
  }
}
```

### Delete Event Image

**Endpoint**: `DELETE /api/events/{eventId}/image`

**Headers**:
- `Authorization: Bearer {jwt-token}`

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "event-uuid",
    "name": "Event Name",
    "imageUrl": null,
    ...
  }
}
```

## Image Specifications

### Supported Formats
- JPEG/JPG
- PNG
- WebP

### Size Limits
- Maximum file size: 10MB
- Maximum dimensions: 1920x1080 pixels
- Images larger than max dimensions are automatically resized

### Optimization
- Aspect ratio is maintained during resizing
- High-quality interpolation for smooth resizing
- Automatic format conversion if needed

## Usage Examples

### cURL Example

```bash
# Upload image
curl -X POST \
  http://localhost:8080/api/events/{eventId}/image \
  -H 'Authorization: Bearer {jwt-token}' \
  -F 'image=@/path/to/image.jpg'

# Delete image
curl -X DELETE \
  http://localhost:8080/api/events/{eventId}/image \
  -H 'Authorization: Bearer {jwt-token}'
```

### JavaScript Example

```javascript
// Upload image
const formData = new FormData();
formData.append('image', imageFile);

const response = await fetch(`/api/events/${eventId}/image`, {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  },
  body: formData
});

const result = await response.json();
console.log('Image URL:', result.data.cdnUrl || result.data.imageUrl);
```

## Error Handling

### Common Errors

**400 Bad Request**
- File is not an image
- File size exceeds 10MB
- Invalid image format
- Image file is corrupted

**401 Unauthorized**
- Missing or invalid JWT token

**403 Forbidden**
- User is not the event organizer

**404 Not Found**
- Event does not exist

**500 Internal Server Error**
- S3 upload failed
- Image processing failed

### Error Response Format

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Image file size exceeds maximum allowed size of 10 MB",
    "timestamp": "2024-01-15T10:30:00Z",
    "requestId": "req_123456789"
  }
}
```

## Best Practices

1. **Image Optimization**: Optimize images before upload to reduce file size
2. **CDN Usage**: Use CloudFront CDN URLs for production to improve performance
3. **Error Handling**: Implement proper error handling for upload failures
4. **Progress Tracking**: Show upload progress for better UX
5. **Image Preview**: Display image preview before upload
6. **Validation**: Validate image format and size on client-side before upload

## Security Considerations

1. **Authentication**: All image operations require valid JWT token
2. **Authorization**: Only event organizers can upload/delete images
3. **File Validation**: Server-side validation prevents malicious file uploads
4. **S3 Permissions**: Bucket configured for public read, but write requires AWS credentials
5. **HTTPS**: All image URLs use HTTPS for secure delivery

## Troubleshooting

### Upload Fails

1. Check AWS credentials are configured correctly
2. Verify S3 bucket exists and is accessible
3. Ensure IAM user has `s3:PutObject` permission
4. Check file size is under 10MB
5. Verify image format is supported

### Images Not Loading

1. Check S3 bucket policy allows public read
2. Verify CORS configuration if accessing from browser
3. Check CloudFront distribution is active
4. Ensure image URL is correct

### Slow Image Loading

1. Enable CloudFront CDN for faster delivery
2. Configure appropriate cache headers
3. Optimize images before upload
4. Use WebP format for better compression
