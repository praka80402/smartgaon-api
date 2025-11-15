//package com.smartgaon.ai.smartgaon_api.comment.controller;
//
//import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
//import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
//import com.smartgaon.ai.smartgaon_api.comment.model.Comment;
//import com.smartgaon.ai.smartgaon_api.comment.service.CommentService;
//import com.smartgaon.ai.smartgaon_api.model.User;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/comments")
//@CrossOrigin(origins = "http://localhost:3000")
//public class CommentController {
//
//    @Autowired private JwtUtil jwtUtil;
//    @Autowired private CommentService commentService;
//    @Autowired private UserRepository userRepo;
//
//    @PostMapping("/{postId}")
//    public ResponseEntity<?> addComment(@RequestHeader("Authorization") String token,
//                                        @PathVariable Long postId,
//                                        @RequestBody Comment comment) {
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//            Optional<User> userOpt = userRepo.findByEmail(email);
//
//            if (userOpt.isEmpty()) {
//                return ResponseEntity.status(404).body("User not found");
//            }
//
//            User user = userOpt.get();
//            comment.setCommenterEmail(email);
//            comment.setCommenterName(user.getFirstName() + " " + user.getLastName());
//
//            Comment saved = commentService.addComment(postId, comment);
//            return ResponseEntity.ok(saved);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//
//    @GetMapping("/{postId}")
//    public List<Comment> getComments(@PathVariable Long postId) {
//        return commentService.getComments(postId);
//    }
//}
//
