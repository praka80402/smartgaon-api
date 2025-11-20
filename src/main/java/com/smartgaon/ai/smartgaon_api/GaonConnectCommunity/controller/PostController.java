package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.CreatePostRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.PostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service.PostService;

@RestController
@RequestMapping("/api/community/posts")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable UUID id,
                                                   @RequestBody CreatePostRequest request) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/pagination")
    public ResponseEntity<List<PostResponse>> getPaginatedPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(postService.getPaginatedPosts(page, limit));
    }

    
    @PostMapping(value = "/upload", consumes = {"multipart/form-data"})
    public ResponseEntity<PostResponse> createPost(
            @RequestParam("topic") String topic,
            @RequestParam("description") String description,
            @RequestParam("shortDescription") String shortDescription,
            @RequestParam("postedBy") String postedBy,
            @RequestParam("tagVillage") String tagVillage,
            @RequestParam("image") MultipartFile image
    ) {
        return ResponseEntity.ok(
                postService.createPostWithImage(
                        topic,
                        description,
                        shortDescription,
                        postedBy,
                        tagVillage,
                        image
                )
        );
    }

}
