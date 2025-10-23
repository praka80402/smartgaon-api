package com.smartgaon.ai.smartgaon_api.controller;

//import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/profile")
//@CrossOrigin(origins = "http://localhost:3000")
//public class ProfileController {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//    @Autowired
//    private UserRepository userRepo;
//
//    @GetMapping
//    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//            Optional<User> user = userRepo.findByEmail(email);
//
//            if (user.isEmpty()) return ResponseEntity.status(404).body("User not found");
//
//            user.get().setPassword(null);
//            return ResponseEntity.ok(user.get());
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//    
//    
//}
//


import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepo;

   
    @GetMapping
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);
            Optional<User> user = userRepo.findByEmail(email);

            if (user.isEmpty())
                return ResponseEntity.status(404).body("User not found");

            user.get().setPassword(null);
            return ResponseEntity.ok(user.get());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody User updatedData) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOpt = userRepo.findByEmail(email);

            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");

            User user = userOpt.get();
            user.setBio(updatedData.getBio());
            user.setVillage(updatedData.getVillage());
            userRepo.save(user);

            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating profile");
        }
    }
    // ✅ Upload or update profile picture
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);
            Optional<User> userOpt = userRepo.findByEmail(email);

            if (userOpt.isEmpty())
                return ResponseEntity.status(404).body("User not found");

            User user = userOpt.get();
            user.setProfileImage(file.getBytes());
            userRepo.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading image");
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }

    // ✅ Fetch profile image (for displaying in React)
    @GetMapping("/image")
    public ResponseEntity<?> getProfileImage(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);

            Optional<User> userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty() || userOpt.get().getProfileImage() == null)
                return ResponseEntity.status(404).body("No profile image found");

            return ResponseEntity.ok()
                    .header("Content-Type", "image/jpeg")
                    .body(userOpt.get().getProfileImage());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}
