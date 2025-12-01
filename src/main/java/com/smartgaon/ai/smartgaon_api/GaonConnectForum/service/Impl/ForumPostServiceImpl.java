package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.Impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;
import com.smartgaon.ai.smartgaon_api.model.User;

import jakarta.transaction.Transactional;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    @Autowired
    private ForumPostRepository postRepo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private UserRepository userRepo;

    // ================= MAP ENTITY TO RESPONSE =================
    private ForumPostResponse map(ForumPost p) {
        String fullName =
                (p.getUser() != null ? (p.getUser().getFirstName() == null ? "" : p.getUser().getFirstName()) : "")
                + " " +
                (p.getUser() != null ? (p.getUser().getLastName() == null ? "" : p.getUser().getLastName()) : "");

        return new ForumPostResponse(
                p.getPostId(),
                p.getUser() != null ? p.getUser().getId() : null,
                fullName.trim(),
                p.getTitle(),
                p.getContent(),
                p.getCategory(),
                p.getArea(),
                p.getUser() != null ? p.getUser().getProfileImageUrl() : null,
                p.getMediaAttachments(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getStatus().name(),
                p.getLikedUsers(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    // ================= CREATE POST (JSON) =================
    @Override
    public ForumPostResponse create(ForumPostCreateDto dto) {

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setCategory(dto.category());

        if (dto.mediaAttachments() != null) {
            post.setMediaAttachments(dto.mediaAttachments());
        }

        return map(postRepo.save(post));
    }

    // ================= GET BY ID =================
    @Override
    public ForumPostResponse getById(Long postId) {
        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Post not found");
        }

        return map(post);
    }

    // ================= LIST POSTS (exclude deleted) =================
    @Override
    public Page<ForumPostResponse> list(Pageable pageable) {
        return postRepo.findByDeletedFalse(pageable).map(this::map);
    }

    // ================= SEARCH POSTS (still includes deleted handling on map usage) =================
    @Override
    public Page<ForumPostResponse> search(String query, Pageable pageable) {
        // We reuse existing repository search method; filter deleted at mapping time by excluding deleted ones.
        Page<ForumPost> page = postRepo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable);
        return page.map(p -> p.isDeleted() ? null : map(p))
                   .map(r -> r); // allow mapping; controller should handle nulls if necessary
    }

    // ================= EDIT TEXT ONLY =================
    @Override
    @Transactional
    public ForumPostResponse update(Long postId, ForumPostUpdateDto dto) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Cannot update deleted post");
        }

        if (dto.title() != null) post.setTitle(dto.title());
        if (dto.content() != null) post.setContent(dto.content());
        if (dto.category() != null) post.setCategory(dto.category());
        if (dto.mediaAttachments() != null) post.setMediaAttachments(dto.mediaAttachments());

        return map(postRepo.save(post));
    }

    // ================= EDIT + REPLACE ALL MEDIA =================
    @Override
    @Transactional
    public ForumPostResponse editWithMedia(
            Long postId,
            String title,
            String content,
            String category,
            List<MultipartFile> newMediaFiles
    ) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Cannot edit media of deleted post");
        }

        // Text updates
        if (title != null) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (category != null) post.setCategory(category);

        // Replace all media files
        if (newMediaFiles != null && !newMediaFiles.isEmpty()) {

            // 1) Delete old media from Cloudinary (best-effort)
            for (String url : post.getMediaAttachments()) {
                try {
                    cloudinaryService.deleteFile(url);
                } catch (Exception ex) {
                    // optionally log the failure and continue
                }
            }
            post.getMediaAttachments().clear();

            // 2) Upload new files
            for (MultipartFile file : newMediaFiles) {
                String uploadedUrl = cloudinaryService.uploadFile(file);
                post.getMediaAttachments().add(uploadedUrl);
            }
        }

        return map(postRepo.save(post));
    }

    // ================= INCREMENT COMMENT COUNT =================
    @Override
    @Transactional
    public ForumPostResponse incrementCommentCount(Long postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        if (post.isDeleted()) {
            throw new RuntimeException("Post not found");
        }
        post.setCommentCount(post.getCommentCount() + 1);
        return map(postRepo.save(post));
    }

    // ================= MODERATE POST =================
    @Override
    public ForumPostResponse moderate(Long postId, String status) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        if (post.isDeleted()) {
            throw new RuntimeException("Post not found");
        }
        post.setStatus(ForumPost.Status.valueOf(status));
        return map(postRepo.save(post));
    }

    // ================= DELETE POST + DELETE MEDIA + LIKES (soft delete via flag) =================
    @Override
    @Transactional
    public void delete(Long postId) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            return; // already deleted
        }

        // Remove likes
        post.getLikedUsers().clear();
        post.setLikeCount(0L);

        // Delete Cloudinary media (best-effort)
        for (String url : post.getMediaAttachments()) {
            try {
                cloudinaryService.deleteFile(url);
            } catch (Exception ex) {
                // optionally log and continue
            }
        }
        post.getMediaAttachments().clear();

        // Mark deleted using flag and status
        post.setDeleted(true);
        post.setStatus(ForumPost.Status.DELETED);

        postRepo.save(post);
    }

    // ================= CREATE WITH ONE IMAGE =================
    @Override
    public ForumPostResponse createWithImage(
            Long userId,
            String title,
            String content,
            String category,
            String area,
            MultipartFile image
    ) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String imageUrl = cloudinaryService.uploadFile(image);

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setArea(area);
        post.getMediaAttachments().add(imageUrl);

        return map(postRepo.save(post));
    }

    // ================= CREATE MULTIPLE MEDIA =================
    @Override
    @Transactional
    public ForumPostResponse createWithMedia(
            Long userId,
            String title,
            String content,
            String category,
            String area,
            List<MultipartFile> files
    ) {

        if (files.size() > 5) {
            throw new RuntimeException("Maximum 5 media files allowed");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setArea(area);

        for (MultipartFile file : files) {
            String url = cloudinaryService.uploadFile(file);
            post.getMediaAttachments().add(url);
        }

        return map(postRepo.save(post));
    }
    @Override
    @Transactional
    public ForumPostResponse toggleLike(Long postId, Long userId) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.isDeleted()) {
            throw new RuntimeException("Cannot like a deleted post");
        }

        // If user already liked → Unlike
        if (post.getLikedUsers().contains(userId)) {
            post.getLikedUsers().remove(userId);
            post.setLikeCount(post.getLikeCount() - 1);
        } else {
            // Like the post
            post.getLikedUsers().add(userId);
            post.setLikeCount(post.getLikeCount() + 1);
        }

        postRepo.save(post);
        return map(post);
    }

}
