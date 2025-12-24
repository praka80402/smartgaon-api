package com.smartgaon.ai.smartgaon_api.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /** Upload file (Cloudinary upload replacement) */
    public String uploadFile(MultipartFile file) {
        try {
            String key = "forum_media/" +
                    UUID.randomUUID() + "-" + file.getOriginalFilename();

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request,
                    RequestBody.fromBytes(file.getBytes()));

            return getPublicUrl(key);

        } catch (IOException e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    /** Delete file (Cloudinary destroy replacement) */
    public boolean deleteFile(String fileUrl) {
        try {
            String key = extractKeyFromUrl(fileUrl);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());

            return true;

        } catch (Exception e) {
            throw new RuntimeException("S3 delete failed", e);
        }
    }

    /** Build public URL (if bucket/object is public) */
    private String getPublicUrl(String key) {
        return "https://" + bucket + ".s3.ap-south-1.amazonaws.com/" + key;
    }

    /** Convert full S3 URL → object key */
    private String extractKeyFromUrl(String url) {
        return url.substring(url.indexOf(".amazonaws.com/") + 14);
    }
}
