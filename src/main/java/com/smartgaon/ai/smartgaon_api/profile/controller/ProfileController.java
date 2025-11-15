//package com.smartgaon.ai.smartgaon_api.controller;
//
////import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
////import com.smartgaon.ai.smartgaon_api.model.User;
////import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import java.util.Optional;
////
////@RestController
////@RequestMapping("/api/profile")
////@CrossOrigin(origins = "http://localhost:3000")
////public class ProfileController {
////
////    @Autowired
////    private JwtUtil jwtUtil;
////    @Autowired
////    private UserRepository userRepo;
////
////    @GetMapping
////    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
////        try {
////            if (token.startsWith("Bearer ")) token = token.substring(7);
////            String email = jwtUtil.extractEmail(token);
////            Optional<User> user = userRepo.findByEmail(email);
////
////            if (user.isEmpty()) return ResponseEntity.status(404).body("User not found");
////
////            user.get().setPassword(null);
////            return ResponseEntity.ok(user.get());
////        } catch (Exception e) {
////            return ResponseEntity.status(401).body("Invalid token");
////        }
////    }
////    
////    
////}
////
//
//
//import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import java.util.Map;
//import java.io.IOException;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/profile")
//@CrossOrigin(origins = "http://localhost:3000")
//public class ProfileController {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    @Autowired
//    private UserRepository userRepo;
//
//    @GetMapping
//    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
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
//            user.setPassword(null);
//            return ResponseEntity.ok(user); 
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//
//
//   
//
////    
//    @PutMapping("/update")
//    public ResponseEntity<?> updateProfile(
//            @RequestHeader("Authorization") String token,
//            @RequestBody User updatedData) {
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//            Optional<User> userOpt = userRepo.findByEmail(email);
//
//            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");
//
//            User user = userOpt.get();
//
//            // ✅ Update editable fields
//            if (updatedData.getFirstName() != null) user.setFirstName(updatedData.getFirstName());
//            if (updatedData.getLastName() != null) user.setLastName(updatedData.getLastName());
//            if (updatedData.getEmail() != null) user.setEmail(updatedData.getEmail());
//            if (updatedData.getBio() != null) user.setBio(updatedData.getBio());
//            if (updatedData.getVillage() != null) user.setVillage(updatedData.getVillage());
//            if (updatedData.getOccupation() != null) user.setOccupation(updatedData.getOccupation());
//
//            userRepo.save(user);
//
//            return ResponseEntity.ok("Profile updated successfully!");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error updating profile");
//        }
//    }
//
//
//    @PutMapping("/change-password")
//    public ResponseEntity<?> changePassword(
//            @RequestHeader("Authorization") String token,
//            @RequestBody Map<String, String> passwords) {
//
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//
//            Optional<User> userOpt = userRepo.findByEmail(email);
//            if (userOpt.isEmpty()) {
//                return ResponseEntity.status(404).body("User not found");
//            }
//
//            User user = userOpt.get();
//            String currentPassword = passwords.get("currentPassword");
//            String newPassword = passwords.get("newPassword");
//
//          
//            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//            if (!encoder.matches(currentPassword, user.getPassword())) {
//                return ResponseEntity.status(400).body("❌ Current password is incorrect");
//            }
//
//          
//            user.setPassword(encoder.encode(newPassword));
//            userRepo.save(user);
//
//            return ResponseEntity.ok("✅ Password changed successfully!");
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error changing password");
//        }
//    }
//
//
//    @PostMapping("/upload-image")
//    public ResponseEntity<?> uploadImage(
//            @RequestHeader("Authorization") String token,
//            @RequestParam("file") MultipartFile file) {
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//            Optional<User> userOpt = userRepo.findByEmail(email);
//
//            if (userOpt.isEmpty())
//                return ResponseEntity.status(404).body("User not found");
//
//            User user = userOpt.get();
//            user.setProfileImage(file.getBytes());
//            userRepo.save(user);
//
//            return ResponseEntity.ok("Profile image uploaded successfully!");
//        } catch (IOException e) {
//            return ResponseEntity.status(500).body("Error uploading image");
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//
//   
//    @GetMapping("/image")
//    public ResponseEntity<?> getProfileImage(@RequestHeader("Authorization") String token) {
//        try {
//            if (token.startsWith("Bearer ")) token = token.substring(7);
//            String email = jwtUtil.extractEmail(token);
//
//            Optional<User> userOpt = userRepo.findByEmail(email);
//            if (userOpt.isEmpty() || userOpt.get().getProfileImage() == null)
//                return ResponseEntity.status(404).body("No profile image found");
//
//            return ResponseEntity.ok()
//                    .header("Content-Type", "image/jpeg")
//                    .body(userOpt.get().getProfileImage());
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid token");
//        }
//    }
//    
//    @GetMapping("/image/{userId}")
//    public ResponseEntity<?> getProfileImageById(@PathVariable Long userId) {
//        try {
//            Optional<User> userOpt = userRepo.findById(userId);
//
//            if (userOpt.isEmpty() || userOpt.get().getProfileImage() == null) {
//                return ResponseEntity.status(404).body("No profile image found");
//            }
//
//            return ResponseEntity.ok()
//                    .header("Content-Type", "image/jpeg")
//                    .body(userOpt.get().getProfileImage());
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error fetching image");
//        }
//    }
//    
//
//
//    
//    
//}

//-------------------------------------------------------------------------------



package com.smartgaon.ai.smartgaon_api.profile.controller;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.Base64;


@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{phone}")
    public ResponseEntity<?> getProfile(@PathVariable String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }
        User user = userOpt.get();

        // ⭐ Combine firstName + lastName
        String fullName =
                (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                (user.getLastName() == null ? "" : user.getLastName());
        
        // ⭐ Send custom structured response with fullName
        return ResponseEntity.ok(
                new java.util.HashMap<String, Object>() {{
                    put("firstName", user.getFirstName());
                    put("lastName", user.getLastName());
                    put("fullName", fullName.trim());
                    put("phone", user.getPhone());
                    put("email", user.getEmail());
                    put("pincode", user.getPincode());
                    put("area", user.getArea());
                    put("village", user.getVillage());
                    put("profileImage", user.getProfileImage());
                    put("profileCompleted", user.isProfileCompleted());
                }}
        );
        
//        return ResponseEntity.ok(userOpt.get());
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {
        Optional<User> userOpt = userRepository.findByPhone(updatedUser.getPhone());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPincode(updatedUser.getPincode());
        user.setArea(updatedUser.getArea());
        user.setVillage(updatedUser.getVillage());
//        user.setOccupation(updatedUser.getOccupation());
//        user.setBio(updatedUser.getBio());
        user.setProfileCompleted(true);

        userRepository.save(user);
        return ResponseEntity.ok(user);
    }
    
    
    @PostMapping("/upload-image/{phone}")
    public ResponseEntity<?> uploadImage(
            @PathVariable String phone,
            @RequestParam("file") MultipartFile file) {

        try {
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = userOpt.get();
            user.setProfileImage(file.getBytes());
            userRepository.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading image");
        }
    }

    // 🔹 GET IMAGE BY PHONE
    @GetMapping("/image/{phone}")
    public ResponseEntity<?> getProfileImage(@PathVariable String phone) {
        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty() || userOpt.get().getProfileImage() == null) {
            return ResponseEntity.status(404).body("No profile image found");
        }
        
        byte[] imgBytes = userOpt.get().getProfileImage();
        String base64Image = Base64.getEncoder().encodeToString(imgBytes);

        return ResponseEntity.ok(base64Image);

//        return ResponseEntity.ok()
//                .header("Content-Type", "image/jpeg")
//                .body(userOpt.get().getProfileImage());
    }
}














