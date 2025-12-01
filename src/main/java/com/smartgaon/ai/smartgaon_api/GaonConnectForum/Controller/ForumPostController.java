package com.smartgaon.ai.smartgaon_api.GaonConnectForum.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;

@RestController
@RequestMapping("/api/forum/posts")
public class ForumPostController {

    @Autowired
    private ForumPostService postService;

    // ------------ CREATE POST JSON -----------
    @PostMapping
    public ForumPostResponse create(@RequestBody ForumPostCreateDto dto) {
        return postService.create(dto);
    }

    // ------------ GET BY ID ----------
    @GetMapping("/{id}")
    public ForumPostResponse getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    // ------------ LIST POSTS ----------
    @GetMapping
    public Page<ForumPostResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postService.list(pageable);
    }

    // ------------ SEARCH ----------
    @GetMapping("/search")
    public Page<ForumPostResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.search(query, pageable);
    }

    // ------------ EDIT TEXT ONLY (JSON) ----------
    @PutMapping("/{id}")
    public ForumPostResponse update(
            @PathVariable Long id,
            @RequestBody ForumPostUpdateDto dto
    ) {
        return postService.update(id, dto);
    }

    // ------------ EDIT MEDIA (Replace All Media Files) ----------
    @PutMapping(value = "/{id}/edit-media", consumes = "multipart/form-data")
    public ForumPostResponse editMedia(
            @PathVariable Long id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String category,
            @RequestParam("newMediaFiles") List<MultipartFile> newMediaFiles
    ) {
        return postService.editWithMedia(id, title, content, category, newMediaFiles);
    }

    // ------------ LIKE/UNLIKE ----------
    @PostMapping("/{postId}/toggle-like")
    public ResponseEntity<ForumPostResponse> toggleLike(
            @PathVariable Long postId,
            @RequestParam Long userId
    ) {
        ForumPostResponse response = postService.toggleLike(postId, userId);
        return ResponseEntity.ok(response);
    }

    // ------------ INCREMENT COMMENT COUNT ----------
    @PostMapping("/{id}/comment-count")
    public ForumPostResponse incrementComment(@PathVariable Long id) {
        return postService.incrementCommentCount(id);
    }

    // ------------ MODERATE POST ----------
    @PostMapping("/{id}/moderate")
    public ForumPostResponse moderate(@PathVariable Long id, @RequestParam String status) {
        return postService.moderate(id, status);
    }

    // ------------ DELETE POST ----------
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }

    // ------------ CREATE WITH SINGLE IMAGE ----------
    @PostMapping(value = "/create-with-image", consumes = "multipart/form-data")
    public ForumPostResponse createWithImage(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam(required = false) String area,
            @RequestParam("image") MultipartFile image
    ) {
        return postService.createWithImage(userId, title, content, category, area, image);
    }

    // ------------ CREATE WITH MULTIPLE MEDIA ----------
    @PostMapping(value = "/create-multi", consumes = "multipart/form-data")
    public ForumPostResponse createWithMedia(
            @RequestParam Long userId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String category,
            @RequestParam(required = false) String area,
            @RequestParam("media") List<MultipartFile> mediaFiles
    ) {
        return postService.createWithMedia(userId, title, content, category, area, mediaFiles);
    }
}
