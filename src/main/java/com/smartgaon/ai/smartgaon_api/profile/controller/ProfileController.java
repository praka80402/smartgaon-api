package com.smartgaon.ai.smartgaon_api.profile.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")

public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cloudinary cloudinary;

    // ------------------------------------------
    // GET PROFILE BY PHONE
    // ------------------------------------------
    @GetMapping("/{phone}")
    public ResponseEntity<?> getProfile(@PathVariable String phone) {

        Optional<User> userOpt = userRepository.findByPhone(phone);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();

        String fullName =
                (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                (user.getLastName() == null ? "" : user.getLastName());

        return ResponseEntity.ok(
                new java.util.HashMap<String, Object>() {{
                    put("id", user.getId());
                    put("firstName", user.getFirstName());
                    put("lastName", user.getLastName());
                    put("fullName", fullName.trim());
                    put("phone", user.getPhone());
                    put("email", user.getEmail());
                    put("roles", user.getRoles());            
                    put("pincode", user.getPincode());
                    put("area", user.getArea());
                    put("village", user.getVillage());
                    put("district", user.getDistrict());
                    put("state", user.getState());
                    put("profileImageUrl", user.getProfileImageUrl());
                    put("profileCompleted", user.isProfileCompleted());
                }}
        );
    }

    // ------------------------------------------
    // UPDATE PROFILE
    // ------------------------------------------
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
        user.setVillage(updatedUser.getVillage());
        user.setDistrict(updatedUser.getDistrict());
        user.setState(updatedUser.getState());
        user.setRoles(updatedUser.getRoles());        // ⭐ ADDED ROLES
        user.setProfileCompleted(true);

        userRepository.save(user);

        return ResponseEntity.ok(user);
    }
// ------------------------------------------
// UPLOAD IMAGE TO CLOUDINARY
// ------------------------------------------
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

        // ----------------------------
        // CLOUDINARY UPLOAD (SAME AS IN PROBLEM REPORT)
        // ----------------------------
        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", "user_profiles")
        );

        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl == null) {
            return ResponseEntity.status(500).body("Failed to upload image");
        }

        // Save only URL
        user.setProfileImageUrl(secureUrl.toString());
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Profile image uploaded successfully!",
                        "url", secureUrl.toString()
                )
        );

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error uploading image: " + e.getMessage());
    }
}


// ------------------------------------------
// GET IMAGE URL (no base64, very FAST)
// ------------------------------------------
@GetMapping("/image/{phone}")
public ResponseEntity<?> getProfileImage(@PathVariable String phone) {

    Optional<User> userOpt = userRepository.findByPhone(phone);

    if (userOpt.isEmpty() || userOpt.get().getProfileImageUrl() == null) {
        return ResponseEntity.status(404).body("No profile image found");
    }

    return ResponseEntity.ok(Map.of(
            "url", userOpt.get().getProfileImageUrl()
    ));
}

}
