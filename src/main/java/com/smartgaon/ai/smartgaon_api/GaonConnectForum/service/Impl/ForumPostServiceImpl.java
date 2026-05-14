package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.Impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.*;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPostReport;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ReportReason;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostReportRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import jakarta.transaction.Transactional;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    private static final Collection<ForumPost.Status> VISIBLE_STATUSES =
            List.of(ForumPost.Status.APPROVED, ForumPost.Status.ACTIVE, ForumPost.Status.MODERATED);

    @Autowired
    private ForumPostRepository postRepo;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserRepository userRepo;
    
    @Autowired
    private ForumPostReportRepository reportRepo;

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
                p.getYoutubeVideoUrl(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getStatus().name(),
                p.getLikedUsers(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

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

        post.setStatus(ForumPost.Status.PENDING);

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

        if (!VISIBLE_STATUSES.contains(post.getStatus())) {
            throw new RuntimeException("Post not found");
        }

        return map(post);
    }

    // ================= LIST POSTS (exclude deleted) =================
    @Override
    public Page<ForumPostResponse> list(Pageable pageable) {
        return postRepo.findVisiblePosts(VISIBLE_STATUSES, pageable).map(this::map);
    }

    // ================= SEARCH POSTS (still includes deleted handling on map usage) =================
    @Override
    public Page<ForumPostResponse> search(String query, Pageable pageable) {
        return postRepo.searchVisiblePosts(query, VISIBLE_STATUSES, pageable).map(this::map);
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
        if (dto.youtubeVideoUrl() != null) {
            String trimmed = dto.youtubeVideoUrl().trim();
            post.setYoutubeVideoUrl(trimmed.isEmpty() ? null : trimmed);
        }

        post.setStatus(ForumPost.Status.PENDING);

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
            String youtubeVideoUrl
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
        if (youtubeVideoUrl != null) {
            String trimmed = youtubeVideoUrl.trim();
            if (!trimmed.isEmpty() && !isYouTubeUrl(trimmed)) {
                throw new RuntimeException("Please provide a valid YouTube URL");
            }
            post.setYoutubeVideoUrl(trimmed.isEmpty() ? null : trimmed);
        }

        post.setStatus(ForumPost.Status.PENDING);

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
        post.setStatus(ForumPost.Status.valueOf(status.trim().toUpperCase(Locale.ROOT)));
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
                s3Service.deleteFile(url);
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

        String imageUrl = s3Service.uploadFile(image);

        ForumPost post = new ForumPost();
        post.setUser(user);
        post.setTitle(title);
        post.setContent(content);
        post.setCategory(category);
        post.setArea(area);
        post.setStatus(ForumPost.Status.PENDING);
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
            List<MultipartFile> files,
            String youtubeVideoUrl
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
        post.setStatus(ForumPost.Status.PENDING);

        if (youtubeVideoUrl != null && !youtubeVideoUrl.trim().isEmpty()) {
            String trimmed = youtubeVideoUrl.trim();
            if (!isYouTubeUrl(trimmed)) {
                throw new RuntimeException("Please provide a valid YouTube URL");
            }
            post.setYoutubeVideoUrl(trimmed);
        }

        for (MultipartFile file : files) {
            String url = s3Service.uploadFile(file);
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
    
//   ------------------------ 
    @Transactional
    @Override
    public ForumPostResponse reportPost(
            Long postId,
            Long userId,
            ReportReason reason,
            String customReason
    ) {

        // Get post
        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // ❌ User cannot report own post
        if (post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You cannot report your own post");
        }

        // ❌ User cannot report same post twice
        if (reportRepo.existsByPost_PostIdAndReportedByUserId(postId, userId)) {
            throw new RuntimeException("You already reported this post");
        }

        // ✅ Save report
        ForumPostReport report = new ForumPostReport();
        report.setPost(post);
        report.setReportedByUserId(userId);
        report.setReason(reason);
        report.setCustomReason(customReason);

        reportRepo.save(report);

        // ✅ Count reports
        long count = reportRepo.countByPost_PostId(postId);

       
        if (count >= 6 && !post.isDeleted()) {
            post.setDeleted(true);
            post.setStatus(ForumPost.Status.DELETED);
            postRepo.save(post);
        }

        // ✅ Return updated post
        return map(post);
    }

    
    @Override
    public Page<ForumPostResponse> listVisiblePostsForUser(
            Long userId,
            Pageable pageable
    ) {
        return postRepo
                .findVisiblePostsForUser(userId, VISIBLE_STATUSES, pageable)
                .map(this::map);
    }
    private boolean isYouTubeUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            return host.equals("youtube.com")
                    || host.endsWith(".youtube.com")
                    || host.equals("youtu.be")
                    || host.endsWith(".youtu.be");
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
