package com.smartgaon.ai.smartgaon_api.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import java.util.Arrays;
import java.util.Collections;
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


    /* ================= MY BUSINESSES (PAGINATED) ================= */
    public List<BusinessResponse> myBusinesses(
            Long userId,
            int limit,
            int offset
    ) throws Exception {

        return repo.findMyBusinesses(userId, limit, offset)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /* ================= PUBLIC BUSINESSES ================= */
    public List<BusinessResponse> publicBusinesses(
            int limit,
            int offset
    ) throws Exception {

        return repo.findPublicBusinesses(limit, offset)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /* ================= UPDATE ================= */
    public BusinessPost update(
            Long businessId,
            Long userId,
            String title,
            String description,
            String location,
            String budget
    ) {

        BusinessPost post = repo.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        post.setTitle(title);
        post.setDescription(description);
        post.setLocation(location);
        post.setBudget(budget);

        return repo.save(post);
    }

    /* ================= DELETE ================= */
    public void delete(Long businessId, Long userId) throws Exception {

        BusinessPost post = repo.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // delete images from S3
        List<String> images = objectMapper.readValue(
                post.getImages(),
                new TypeReference<List<String>>() {}
        );

        for (String img : images) {
            s3Service.deleteFile(img);
        }

        repo.delete(post);
    }

    /* ================= MAPPER ================= */
    private BusinessResponse mapToResponse(BusinessPost b) {
        try {
            List<String> images = List.of();

            if (b.getImages() != null && !b.getImages().isBlank()) {
                images = objectMapper.readValue(
                        b.getImages(),
                        new TypeReference<List<String>>() {}
                );
            }

            return new BusinessResponse(
                    b.getId(),
                    b.getTitle(),
                    b.getDescription(),
                    b.getLocation(),
                    b.getBudget(),   // ✅ correct position
                    images,          // ✅ correct position
                    b.getStatus()    // ✅ correct position
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to map business response for id=" + b.getId(),
                    e
            );
        }
    }


    /**
     * Fetch businesses for owner
     */
//    public List<BusinessResponse> myBusinesses(Long userId) {
//
//        return repo.findByUserIdOrderByCreatedAtDesc(userId)
//                .stream()
//                .map(b -> {
//                    try {
//                        List<String> images = objectMapper.readValue(
//                                b.getImages(),
//                                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}
//                        );
//
//                        return new BusinessResponse(
//                                b.getId(),
//                                b.getTitle(),
//                                b.getDescription(),
//                                b.getLocation(),
//                                b.getBudget(),
//                                images,
//                                b.getStatus()
//                        );
//
//                    } catch (Exception e) {
//                        throw new RuntimeException(
//                                "Failed to parse images for businessId=" + b.getId(),
//                                e
//                        );
//                    }
//                })
//                .toList();
//    }
}
