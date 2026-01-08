package com.smartgaon.ai.smartgaon_api.successstory.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.s3.S3Service;
import com.smartgaon.ai.smartgaon_api.successstory.model.*;
import com.smartgaon.ai.smartgaon_api.successstory.repository.SuccessStoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SuccessStoryService {

    private final SuccessStoryRepository repo;
    private final S3Service s3Service;

    // CREATE
    public SuccessStory create(SuccessStory story) {
        story.setCreatedAt(LocalDateTime.now());
        story.setPublished(true);
        return repo.save(story);
    }

    // EDIT
    public SuccessStory update(
            Long id,
            String title,
            String userName,
            String storyText,
            State state,
            String pincode,
            MultipartFile profileImage
    ) {
        SuccessStory story = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Success story not found"));

        story.setTitle(title);
        story.setUserName(userName);
        story.setStory(storyText);
        story.setState(state);
        story.setPincode(pincode);

        if (profileImage != null && !profileImage.isEmpty()) {
            s3Service.deleteFile(story.getProfileImageUrl());
            String imageUrl = s3Service.uploadFile(profileImage);
            story.setProfileImageUrl(imageUrl);
        }

        return repo.save(story);
    }

    // LIST
    public List<SuccessStory> getAllPublished() {
        return repo.findByPublishedTrueOrderByCreatedAtDesc();
    }
    
    public Optional<SuccessStory> getById(Long id) {
        return repo.findById(id)
                .filter(SuccessStory::isPublished);
    }

    // DELETE
    public void delete(Long id) {
        SuccessStory story = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Success story not found"));

        s3Service.deleteFile(story.getProfileImageUrl());
        repo.delete(story);
    }
}
