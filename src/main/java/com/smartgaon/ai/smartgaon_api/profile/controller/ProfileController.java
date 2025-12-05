package com.smartgaon.ai.smartgaon_api.profile.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    // ============================================
    // GET PROFILE
    // ============================================
    @GetMapping("/{phone}")
    public ResponseEntity<?> getProfile(@PathVariable String phone) {

        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("fullName", (user.getFirstName() + " " + user.getLastName()).trim());
        response.put("phone", user.getPhone());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles());
        response.put("state", user.getState());
        response.put("district", user.getDistrict());
        response.put("area", user.getArea());
        response.put("pincode", user.getPincode());
        response.put("profileImageUrl", user.getProfileImageUrl());
        response.put("profileCompleted", user.isProfileCompleted());

        return ResponseEntity.ok(response);
    }

    // ============================================
    // UPDATE PROFILE
    // ============================================
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {

        Optional<User> userOpt = userRepository.findByPhone(updatedUser.getPhone());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setRoles(updatedUser.getRoles());
        user.setState(updatedUser.getState());
        user.setDistrict(updatedUser.getDistrict());
        user.setArea(updatedUser.getArea());
        user.setPincode(updatedUser.getPincode());

        // Profile completion logic
        boolean completed =
                notEmpty(user.getFirstName()) &&
                notEmpty(user.getLastName()) &&
                notEmpty(user.getState()) &&
                notEmpty(user.getDistrict()) &&
                notEmpty(user.getArea()) &&
                notEmpty(user.getPincode()) &&
                notEmpty(user.getProfileImageUrl());

        user.setProfileCompleted(completed);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Profile updated successfully",
                        "profileCompleted", completed
                )
        );
    }

    // ============================================
    // UPLOAD PROFILE IMAGE
    // ============================================
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

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "user_profiles")
            );

            String secureUrl = uploadResult.get("secure_url").toString();
            user.setProfileImageUrl(secureUrl);

            boolean completed =
                    notEmpty(user.getFirstName()) &&
                    notEmpty(user.getLastName()) &&
                    notEmpty(user.getState()) &&
                    notEmpty(user.getDistrict()) &&
                    notEmpty(user.getArea()) &&
                    notEmpty(user.getPincode()) &&
                    notEmpty(user.getProfileImageUrl());

            user.setProfileCompleted(completed);

            userRepository.save(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Profile image uploaded successfully!",
                            "url", secureUrl,
                            "profileCompleted", completed
                    )
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
        }
    }

    // ============================================
    // GET PROFILE IMAGE
    // ============================================
    @GetMapping("/image/{phone}")
    public ResponseEntity<?> getProfileImage(@PathVariable String phone) {

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty() || userOpt.get().getProfileImageUrl() == null) {
            return ResponseEntity.status(404).body("No profile image found");
        }

        return ResponseEntity.ok(Map.of("url", userOpt.get().getProfileImageUrl()));
    }

    // Utility method
    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
