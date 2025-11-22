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



public class ProfileController {

    @Autowired
    private UserRepository userRepository;

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
                    put("profileImage", user.getProfileImage());
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
    // UPLOAD IMAGE
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
            user.setProfileImage(file.getBytes());
            userRepository.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully!");

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error uploading image");
        }
    }

    // ------------------------------------------
    // GET IMAGE BY PHONE
    // ------------------------------------------
    @GetMapping("/image/{phone}")
    public ResponseEntity<?> getProfileImage(@PathVariable String phone) {

        Optional<User> userOpt = userRepository.findByPhone(phone);

        if (userOpt.isEmpty() || userOpt.get().getProfileImage() == null) {
            return ResponseEntity.status(404).body("No profile image found");
        }

        byte[] imgBytes = userOpt.get().getProfileImage();
        String base64Image = Base64.getEncoder().encodeToString(imgBytes);

        return ResponseEntity.ok(base64Image);
    }
}
