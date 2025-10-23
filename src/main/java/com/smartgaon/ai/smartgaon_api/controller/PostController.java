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
//

//----------------------------------------------

//package com.smartgaon.ai.smartgaon_api.controller;
//
//import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
//import com.smartgaon.ai.smartgaon_api.model.Post;
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
//import com.smartgaon.ai.smartgaon_api.service.PostService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/posts")
//@CrossOrigin(origins = "http://localhost:3000")
//public class PostController {
//
//    @Autowired private JwtUtil jwtUtil;
//    @Autowired private PostService postService;
//    @Autowired private UserRepository userRepo;
//
//    private final String UPLOAD_DIR = "uploads/";
//
//    // 🧠 Get All Posts
//    @GetMapping
//    public ResponseEntity<?> getAllPosts() {
//        return ResponseEntity.ok(postService.getAllPosts());
//    }
//
//    // ✍️ Create Post
//    @PostMapping("/upload")
//    public ResponseEntity<?> createPostWithMedia(
//            @RequestHeader("Authorization") String token,
//            @RequestParam(value = "content", required = false) String content,
//            @RequestParam(value = "image", required = false) MultipartFile image,
//            @RequestParam(value = "video", required = false) MultipartFile video) {
//
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//            Optional<User> userOpt = userRepo.findByEmail(email);
//
//            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");
//
//            if (content != null && content.length() > 250)
//                return ResponseEntity.badRequest().body("Post cannot exceed 250 characters");
//
//            if (image != null && image.getSize() > 2 * 1024 * 1024)
//                return ResponseEntity.badRequest().body("Image size must be ≤ 2MB");
//
//            if (video != null && video.getSize() > 15 * 1024 * 1024)
//                return ResponseEntity.badRequest().body("Video must be ≤ 15 seconds");
//
//            User user = userOpt.get();
//            Post post = new Post();
//            post.setContent(content);
//            post.setAuthorEmail(email.trim().toLowerCase());
//            post.setAuthorName(user.getFirstName() + " " + user.getLastName());
//            post.setUser(user);
//
//            if (!Files.exists(Paths.get(UPLOAD_DIR))) Files.createDirectories(Paths.get(UPLOAD_DIR));
//
////            if (image != null && !image.isEmpty()) {
////                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
////                Files.copy(image.getInputStream(), Paths.get(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
//////                post.setImageUrl("/uploads/" + fileName);
////                post.setImageUrl(image.getBytes());
////                
////                
////            }
//
//            if (video != null && !video.isEmpty()) {
//                String fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
//                Files.copy(video.getInputStream(), Paths.get(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
//                post.setVideoUrl("/uploads/" + fileName);
//            }
//
//            return ResponseEntity.ok(postService.createPost(post));
//
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("File upload failed");
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//
//    // 🗑️ Delete Post
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String token,
//                                        @PathVariable Long id) {
//        try {
//            System.out.println("\n========== DELETE DEBUG START ==========");
//
//            // 🔹 Step 1: Strip Bearer prefix
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            System.out.println("Raw token: " + token);
//
//            // 🔹 Step 2: Extract email from JWT
//            String email = jwtUtil.extractEmail(token);
//            System.out.println("Email extracted from token: " + email);
//
//            // 🔹 Step 3: Get post from DB
//            Optional<Post> postOpt = postService.getPostById(id);
//            if (postOpt.isEmpty()) {
//                System.out.println("❌ Post not found in DB");
//                return ResponseEntity.status(404).body("Post not found");
//            }
//
//            Post post = postOpt.get();
//            System.out.println("Post ID: " + post.getId());
//            System.out.println("Post authorEmail (DB): " + post.getAuthorEmail());
//
//            // 🔹 Step 4: Compare safely (ignore case and spaces)
//            if (post.getAuthorEmail() == null ||
//                    !post.getAuthorEmail().trim().equalsIgnoreCase(email.trim())) {
//                System.out.println("❌ Emails do not match!");
//                System.out.println("Logged-in user: " + email);
//                System.out.println("Post owner: " + post.getAuthorEmail());
//                System.out.println("========== DELETE DEBUG END ==========\n");
//                return ResponseEntity.status(403).body("You can only delete your own post");
//            }
//
//            // 🔹 Step 5: If match → delete
//            System.out.println("✅ Authorized! Deleting post ID " + id);
//            postService.deletePost(id);
//            System.out.println("========== DELETE DEBUG END ==========\n");
//
//            return ResponseEntity.ok("Post deleted successfully");
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(401).body("Invalid token: " + e.getMessage());
//        }
//    }
//
////    @DeleteMapping("/{id}")
////    public ResponseEntity<?> deletePost(@RequestHeader("Authorization") String token, @PathVariable Long id) {
////        try {
////            if (token.startsWith("Bearer ")) token = token.substring(7);
////            String email = jwtUtil.extractEmail(token);
////
////            Optional<Post> postOpt = postService.getPostById(id);
////            if (postOpt.isEmpty()) return ResponseEntity.status(404).body("Post not found");
////
////            Post post = postOpt.get();
////
////            // Debug logs — remove later
////            System.out.println("🔐 Logged-in email: " + email);
////            System.out.println("🧾 Post author email: " + post.getAuthorEmail());
////
////            // Case-insensitive safe comparison
////            if (post.getAuthorEmail() == null ||
////                !post.getAuthorEmail().trim().equalsIgnoreCase(email.trim())) {
////                return ResponseEntity.status(403).body("You can only delete your own post");
////            }
////
////            postService.deletePost(id);
////            return ResponseEntity.ok("✅ Post deleted successfully");
////
////        } catch (Exception e) {
////            return ResponseEntity.status(401).body("Invalid token");
////        }
////    }
//
//    // ✏️ Edit Post
//    @PutMapping("/{id}")
//    public ResponseEntity<?> editPost(@RequestHeader("Authorization") String token,
//                                      @PathVariable Long id,
//                                      @RequestParam(value = "content", required = false) String content,
//                                      @RequestParam(value = "image", required = false) MultipartFile image,
//                                      @RequestParam(value = "video", required = false) MultipartFile video) {
//
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//
//            Optional<Post> postOpt = postService.getPostById(id);
//            if (postOpt.isEmpty()) return ResponseEntity.status(404).body("Post not found");
//
//            Post post = postOpt.get();
//
//            if (post.getAuthorEmail() == null ||
//                !post.getAuthorEmail().trim().equalsIgnoreCase(email.trim())) {
//                return ResponseEntity.status(403).body("You can only edit your own post");
//            }
//
//            if (content != null && content.length() > 250)
//                return ResponseEntity.badRequest().body("Post cannot exceed 250 characters");
//
//            if (image != null && image.getSize() > 2 * 1024 * 1024)
//                return ResponseEntity.badRequest().body("Image size must be ≤ 2MB");
//
//            if (video != null && video.getSize() > 15 * 1024 * 1024)
//                return ResponseEntity.badRequest().body("Video must be ≤ 15 seconds");
//
//            if (content != null) post.setContent(content);
//
//            if (image != null && !image.isEmpty()) {
//                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
//                Files.copy(image.getInputStream(), Paths.get(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
////                post.setImageUrl("/uploads/" + fileName);
//            }
//
//            if (video != null && !video.isEmpty()) {
//                String fileName = System.currentTimeMillis() + "_" + video.getOriginalFilename();
//                Files.copy(video.getInputStream(), Paths.get(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
//                post.setVideoUrl("/uploads/" + fileName);
//            }
//
//            return ResponseEntity.ok(postService.createPost(post));
//
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("File update failed");
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//}
