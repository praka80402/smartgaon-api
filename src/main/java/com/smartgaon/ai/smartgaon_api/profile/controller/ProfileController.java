package com.smartgaon.ai.smartgaon_api.profile.controller;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // ✅ USE S3 instead
    @Autowired
    private S3Service s3Service;



    @GetMapping("/{phone}")
public ResponseEntity<?> getProfileByPhone(@PathVariable String phone) {

    Optional<User> userOpt = userRepository.findByPhone(phone);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(404).body("User not found");
    }

    return ResponseEntity.ok(buildProfileResponse(userOpt.get()));
}

    // ============================================
    // GET PROFILE
    // ============================================
    @GetMapping
    public ResponseEntity<?> getProfile(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {

        Optional<User> userOpt = Optional.empty();

        if (phone != null) {
            userOpt = userRepository.findByPhone(phone);
        }

        if (userOpt.isEmpty() && email != null) {
            userOpt = userRepository.findByEmail(email);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(buildProfileResponse(userOpt.get()));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<?> getProfileById(@PathVariable Long id) {

        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        return ResponseEntity.ok(buildProfileResponse(userOpt.get()));
    }

    // ============================================
    // UPDATE PROFILE
    // ============================================
@PutMapping("/update")
public ResponseEntity<?> updateProfile(@RequestBody User updatedUser) {

    // ✅ Validation: at least one identifier required
    if (updatedUser.getPhone() == null && updatedUser.getEmail() == null) {
        return ResponseEntity.badRequest()
                .body("Phone or Email is required");
    }

    Optional<User> userOpt = Optional.empty();

    // 🔍 Try finding by phone first
    if (updatedUser.getPhone() != null) {
        userOpt = userRepository.findByPhone(updatedUser.getPhone());
    }

    // 🔍 If not found, try by email
    if (userOpt.isEmpty() && updatedUser.getEmail() != null) {
        userOpt = userRepository.findByEmail(updatedUser.getEmail());
    }

    User user;

    // 🔥 AUTO-CREATE (for first-time Gmail login)
    if (userOpt.isEmpty()) {
        user = new User();
        user.setPhone(updatedUser.getPhone());
        user.setEmail(updatedUser.getEmail());
        if (updatedUser.getProfileImageUrl() != null) {
        user.setProfileImageUrl(updatedUser.getProfileImageUrl());
    }
    } else {
        user = userOpt.get();
    }

    // 🔗 Link phone & email if missing
    if (user.getPhone() == null && updatedUser.getPhone() != null) {
        user.setPhone(updatedUser.getPhone());
    }

    if (user.getEmail() == null && updatedUser.getEmail() != null) {
        user.setEmail(updatedUser.getEmail());
    }

    // ✏️ Update profile fields
    user.setFirstName(updatedUser.getFirstName());
    user.setLastName(updatedUser.getLastName());
    user.setRoles(updatedUser.getRoles());
    user.setState(updatedUser.getState());
    user.setDistrict(updatedUser.getDistrict());
    user.setArea(updatedUser.getArea());
    user.setPincode(updatedUser.getPincode());

    // ✅ Profile completion logic
    updateProfileCompletion(user);

    // 💾 Save
    userRepository.save(user);

    return ResponseEntity.ok(
            Map.of(
                    "message", "Profile updated successfully",
                    "profileCompleted", user.isProfileCompleted()
            )
    );
}
    // ============================================
    // UPLOAD PROFILE IMAGE (NOW S3)
    // ============================================
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam("file") MultipartFile file) {

        try {
            Optional<User> userOpt = Optional.empty();

            if (phone != null) {
                userOpt = userRepository.findByPhone(phone);
            }

            if (userOpt.isEmpty() && email != null) {
                userOpt = userRepository.findByEmail(email);
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(404).body("User not found");
            }

            User user = userOpt.get();

            // 🔗 Link missing fields
            if (user.getPhone() == null && phone != null) {
                user.setPhone(phone);
            }
            if (user.getEmail() == null && email != null) {
                user.setEmail(email);
            }

            // 🔥 Upload to S3
            String imageUrl = s3Service.uploadFile(file);

            user.setProfileImageUrl(imageUrl);
            updateProfileCompletion(user);

            userRepository.save(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Profile image uploaded successfully!",
                            "url", imageUrl,
                            "profileCompleted", user.isProfileCompleted()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Image upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/update-gaon-sathi-avatar")
    public ResponseEntity<?> updateGaonSathiAvatar(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email,
            @RequestParam String gaonSathiimageUrl) {

        try {
            Optional<User> userOpt = Optional.empty();

            if (phone != null) {
                userOpt = userRepository.findByPhone(phone);
            }

            if (userOpt.isEmpty() && email != null) {
                userOpt = userRepository.findByEmail(email);
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }

            User user = userOpt.get();

            // 🔗 Link fields
            if (user.getPhone() == null && phone != null) {
                user.setPhone(phone);
            }
            if (user.getEmail() == null && email != null) {
                user.setEmail(email);
            }

            user.setGaonSathiImageUrl(gaonSathiimageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(
                    Map.of(
                            "message", "Gaon Sathi avatar updated successfully",
                            "gaonSathiUrl", gaonSathiimageUrl));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update avatar: " + e.getMessage());
        }
    }

    // ============================================
    // GET PROFILE IMAGE
    // ============================================
    @GetMapping("/image")
    public ResponseEntity<?> getProfileImage(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String email) {

        Optional<User> userOpt = Optional.empty();

        if (phone != null) {
            userOpt = userRepository.findByPhone(phone);
        }

        if (userOpt.isEmpty() && email != null) {
            userOpt = userRepository.findByEmail(email);
        }

        if (userOpt.isEmpty() || userOpt.get().getProfileImageUrl() == null) {
            return ResponseEntity.status(404).body("No profile image found");
        }

        return ResponseEntity.ok(
                Map.of("url", userOpt.get().getProfileImageUrl()));
    }

    // ============================================
    // UTILITIES
    // ============================================
    private void updateProfileCompletion(User user) {
        boolean completed = notEmpty(user.getFirstName()) &&
                notEmpty(user.getLastName()) &&
                notEmpty(user.getState()) &&
                notEmpty(user.getDistrict()) &&
                notEmpty(user.getArea()) &&
                notEmpty(user.getPincode()) &&
                notEmpty(user.getProfileImageUrl());

        user.setProfileCompleted(completed);
    }

    private boolean notEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private Map<String, Object> buildProfileResponse(User user) {
        Map<String, Object> response = new HashMap<>();

        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("fullName",
                (user.getFirstName() + " " + user.getLastName()).trim());
        response.put("phone", user.getPhone());
        response.put("email", user.getEmail());
        response.put("roles", user.getRoles());
        response.put("state", user.getState());
        response.put("district", user.getDistrict());
        response.put("area", user.getArea());
        response.put("pincode", user.getPincode());
        response.put("profileImageUrl", user.getProfileImageUrl());
        response.put("profileCompleted", user.isProfileCompleted());
        response.put("gaonsathi_image_url", user.getGaonSathiImageUrl());

        return response;
    }
}
