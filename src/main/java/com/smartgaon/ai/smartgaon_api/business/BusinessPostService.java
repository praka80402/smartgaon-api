package com.smartgaon.ai.smartgaon_api.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class BusinessPostService {

    private final BusinessPostRepository repo;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    /**
     * Create business with multiple images
     */
    public BusinessPost create(
            Long userId,
            String title,
            String description,
            String location,
            String budget,
            MultipartFile[] images
    ) {

        try {
            // 1️⃣ Upload images to S3
            List<String> imageUrls = Arrays.stream(images)
                    .map(s3Service::uploadFile)
                    .toList();

            // 2️⃣ Convert URLs → JSON string
            String imagesJson = objectMapper.writeValueAsString(imageUrls);

            // 3️⃣ Save entity
            BusinessPost post = new BusinessPost();
            post.setUserId(userId);
            post.setTitle(title);
            post.setBudget(budget);
            post.setDescription(description);
            post.setLocation(location);
            post.setImages(imagesJson); // 👈 JSON STRING

            return repo.save(post);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert images to JSON", e);
        }
    }

    /**
     * Fetch businesses for owner
     */
    public List<BusinessResponse> myBusinesses(Long userId) {

        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(b -> {
                    try {
                        List<String> images = objectMapper.readValue(
                                b.getImages(),
                                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
                        );

                        return new BusinessResponse(
                                b.getId(),
                                b.getTitle(),
                                b.getDescription(),
                                b.getLocation(),
                                b.getBudget(),
                                images,
                                b.getStatus()
                        );

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Failed to parse images for businessId=" + b.getId(),
                                e
                        );
                    }
                })
                .toList();
    }
}
