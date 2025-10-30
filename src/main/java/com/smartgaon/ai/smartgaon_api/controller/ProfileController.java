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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Map;
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

            // ✅ Update editable fields
            if (updatedData.getFirstName() != null) user.setFirstName(updatedData.getFirstName());
            if (updatedData.getLastName() != null) user.setLastName(updatedData.getLastName());
            if (updatedData.getEmail() != null) user.setEmail(updatedData.getEmail());
            if (updatedData.getBio() != null) user.setBio(updatedData.getBio());
            if (updatedData.getVillage() != null) user.setVillage(updatedData.getVillage());
            if (updatedData.getOccupation() != null) user.setOccupation(updatedData.getOccupation());

            userRepo.save(user);

            return ResponseEntity.ok("Profile updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating profile");
        }
    }


    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> passwords) {

        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            String email = jwtUtil.extractEmail(token);

            Optional<User> userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = userOpt.get();
            String currentPassword = passwords.get("currentPassword");
            String newPassword = passwords.get("newPassword");

          
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches(currentPassword, user.getPassword())) {
                return ResponseEntity.status(400).body("❌ Current password is incorrect");
            }

          
            user.setPassword(encoder.encode(newPassword));
            userRepo.save(user);

            return ResponseEntity.ok("✅ Password changed successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error changing password");
        }
    }


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
