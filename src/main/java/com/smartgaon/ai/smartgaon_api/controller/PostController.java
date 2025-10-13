package com.smartgaon.ai.smartgaon_api.controller;

import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.Post;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "http://localhost:3000")
public class PostController {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private PostService postService;
    @Autowired private UserRepository userRepo;

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestHeader("Authorization") String token,
                                        @RequestBody Post post) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOpt = userRepo.findByEmail(email);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = userOpt.get();
            post.setAuthorEmail(email);
            post.setAuthorName(user.getFirstName() + " " + user.getLastName());

            Post saved = postService.createPost(post);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}

