package com.smartgaon.ai.smartgaon_api.profile.controller;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/register-board")
public class RegisterController {

    private final UserRepository userRepository;

    // -------------------------------
    // DTO for Register Board
    // -------------------------------
    @Data
    public static class RegisterRequest {
        private String name;
        private String occupation;
        private String phone;
        private String email;
        private String note;
    }

    // -----------------------------------------------------
    // 1️⃣ REGISTER USER (CREATE OR UPDATE BY PHONE)
    // -----------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<?> registerExpert(@RequestBody RegisterRequest req) {

        Optional<User> existingUserOpt = userRepository.findByPhone(req.getPhone());
        User user = existingUserOpt.orElseGet(User::new);

        // Basic fields
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());

        // Name split
        if (req.getName() != null) {
            String[] parts = req.getName().trim().split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
        }

        // Save occupation + note
        user.setOccupation(req.getOccupation());
        user.setNote(req.getNote());

        // Do NOT modify roles
        // user.setRoles("EXPERT");

        // Mark as profile completed
        user.setProfileCompleted(true);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully",
                        "userId", user.getId()
                )
        );
    }

    // -----------------------------------------------------
    // 2️⃣ FETCH USERS FOR LOCAL EXPERT LIST (NO RATING)
    // -----------------------------------------------------
    @GetMapping("/experts")
    public ResponseEntity<?> getExperts() {

        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> experts = new ArrayList<>();

        for (User user : allUsers) {

            // Only users with occupation
            if (user.getOccupation() == null || user.getOccupation().isBlank()) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();

            String fullName = (user.getFirstName() == null ? "" : user.getFirstName()) + " " +
                              (user.getLastName() == null ? "" : user.getLastName());

            map.put("id", user.getId());
            map.put("fullName", fullName.trim());
            map.put("occupation", user.getOccupation());
            map.put("phone", user.getPhone());
            map.put("email", user.getEmail());
            map.put("note", user.getNote());
            map.put("profileImageUrl", user.getProfileImageUrl());

            experts.add(map);
        }

        return ResponseEntity.ok(experts);
    }
}
