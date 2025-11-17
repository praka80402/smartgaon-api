//package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.Impl;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.data.domain.*;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostRepository;
//import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;
//import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
//import com.smartgaon.ai.smartgaon_api.model.User;
//
//import jakarta.transaction.Transactional;
//
//@Service
//public class ForumPostServiceImpl implements ForumPostService{
//	
//
//    @Autowired
//    private ForumPostRepository postRepo;
//
//    @Autowired
//    private UserRepository userRepo;
//
//    private ForumPostResponse map(ForumPost p) {
//        String fullName = p.getUser().getFirstName() + " " + p.getUser().getLastName();
//
//        return new ForumPostResponse(
//                p.getPostId(),
//                p.getUser().getId(),
//                fullName,
//                p.getTitle(),
//                p.getContent(),
//                p.getCategory(),
//                p.getArea(),
////                p.getTags(),
//                p.getMediaAttachments(),
//                p.getLikeCount(),
//                p.getCommentCount(),
//                p.getStatus().name(),
//                p.getCreatedAt(),
//                p.getUpdatedAt()
//        );
//    }
//
//    @Override
//    public ForumPostResponse create(ForumPostCreateDto dto) {
//
//        User user = userRepo.findById(dto.userId())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        ForumPost post = new ForumPost();
//        post.setUser(user);
//        post.setTitle(dto.title());
//        post.setContent(dto.content());
//        post.setCategory(dto.category());
//        if (dto.tags() != null) post.setTags(dto.tags());
//        if (dto.mediaAttachments() != null) post.setMediaAttachments(dto.mediaAttachments());
//
//        return map(postRepo.save(post));
//    }
//
//    @Override
//    public ForumPostResponse getById(Long postId) {
//        return postRepo.findById(postId).map(this::map)
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//    }
//
//    @Override
//    public Page<ForumPostResponse> list(Pageable pageable) {
//        return postRepo.findAll(pageable).map(this::map);
//    }
//
//    @Override
//    public Page<ForumPostResponse> search(String query, Pageable pageable) {
//        return postRepo
//                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable)
//                .map(this::map);
//    }
//
//    @Override
//    @Transactional
//    public ForumPostResponse update(Long postId, ForumPostUpdateDto dto) {
//
//        ForumPost post = postRepo.findById(postId)
//                .orElseThrow(() -> new RuntimeException("Post not found"));
//
//        if (dto.title() != null) post.setTitle(dto.title());
//        if (dto.content() != null) post.setContent(dto.content());
//        if (dto.category() != null) post.setCategory(dto.category());
//        if (dto.tags() != null) post.setTags(dto.tags());
//        if (dto.mediaAttachments() != null) post.setMediaAttachments(dto.mediaAttachments());
//
//        return map(postRepo.save(post));
//    }
//
//    @Override
//    @Transactional
//    public ForumPostResponse like(Long postId) {
//        ForumPost post = postRepo.findById(postId).orElseThrow();
//        post.setLikeCount(post.getLikeCount() + 1);
//        return map(postRepo.save(post));
//    }
//
//    @Override
//    @Transactional
//    public ForumPostResponse incrementCommentCount(Long postId) {
//    	 ForumPost post = postRepo.findById(postId).orElseThrow();
//        post.setCommentCount(post.getCommentCount() + 1);
//        return map(postRepo.save(post));
//    }
//
//    @Override
//    public ForumPostResponse moderate(Long postId, String status) {
//    	 ForumPost post = postRepo.findById(postId).orElseThrow();
//        post.setStatus( ForumPost.Status.valueOf(status));
//        return map(postRepo.save(post));
//    }
//
//    @Override
//    public void delete(Long postId) {
//    	 ForumPost post = postRepo.findById(postId).orElseThrow();
//        post.setStatus( ForumPost.Status.DELETED);
//        postRepo.save(post);
//    }
//	
//
//}

package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.Impl;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

import jakarta.transaction.Transactional;

@Service
public class ForumPostServiceImpl implements ForumPostService {

    @Autowired
    private ForumPostRepository postRepo;

    @Autowired
    private UserRepository userRepo;

//    private ForumPostResponse map(ForumPost p) {
//        String fullName = p.getUser().getFirstName() + " " + p.getUser().getLastName();
//       
//        
//
//        return new ForumPostResponse(
//                p.getPostId(),
//                p.getUser().getId(),
//                fullName,
//                p.getTitle(),
//                p.getContent(),
//                p.getCategory(),
//                p.getUser().getProfileImage(),
//                p.getArea(),                       // ⭐ area added
//                p.getMediaAttachments(),
//                p.getLikeCount(),
//                p.getCommentCount(),
//                p.getStatus().name(),
//                p.getCreatedAt(),
//                p.getUpdatedAt()
//        );
//    }

    private ForumPostResponse map(ForumPost p) {

        String fullName = (p.getUser().getFirstName() == null ? "" : p.getUser().getFirstName())
                + " " +
                (p.getUser().getLastName() == null ? "" : p.getUser().getLastName());
        fullName = fullName.trim();

        // Convert profile image bytes → base64 string
        byte[] imgBytes = p.getUser().getProfileImage();
        String profileImageBase64 = null;
        if (imgBytes != null && imgBytes.length > 0) {
            profileImageBase64 = Base64.getEncoder().encodeToString(imgBytes);
        }

        return new ForumPostResponse(
                p.getPostId(),
                p.getUser().getId(),
                fullName,
                p.getTitle(),
                p.getContent(),
                p.getCategory(),
                p.getArea(),                 // ✅ area in correct place
                profileImageBase64,          // ✅ now image in correct place (String)
                p.getMediaAttachments(),
                p.getLikeCount(),
                p.getCommentCount(),
                p.getStatus().name(),
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

        // ⭐ Auto-fill AREA from User Table
        post.setArea(user.getArea());

        // ⭐ Store uploaded media list
        if (dto.mediaAttachments() != null) {
            post.setMediaAttachments(dto.mediaAttachments());
        }

        return map(postRepo.save(post));
    }

    @Override
    public ForumPostResponse getById(Long postId) {
        return postRepo.findById(postId)
                .map(this::map)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Override
    public Page<ForumPostResponse> list(Pageable pageable) {
        return postRepo.findAll(pageable).map(this::map);
    }

    @Override
    public Page<ForumPostResponse> search(String query, Pageable pageable) {
        return postRepo
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(query, query, pageable)
                .map(this::map);
    }

    @Override
    @Transactional
    public ForumPostResponse update(Long postId, ForumPostUpdateDto dto) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (dto.title() != null) post.setTitle(dto.title());
        if (dto.content() != null) post.setContent(dto.content());
        if (dto.category() != null) post.setCategory(dto.category());
        if (dto.mediaAttachments() != null) post.setMediaAttachments(dto.mediaAttachments());

        return map(postRepo.save(post));
    }

    @Override
    @Transactional
    public ForumPostResponse like(Long postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setLikeCount(post.getLikeCount() + 1);
        return map(postRepo.save(post));
    }

    @Override
    @Transactional
    public ForumPostResponse incrementCommentCount(Long postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setCommentCount(post.getCommentCount() + 1);
        return map(postRepo.save(post));
    }

    @Override
    public ForumPostResponse moderate(Long postId, String status) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setStatus(ForumPost.Status.valueOf(status));
        return map(postRepo.save(post));
    }

    @Override
    public void delete(Long postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setStatus(ForumPost.Status.DELETED);
        postRepo.save(post);
    }
}

