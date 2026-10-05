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

    @Data
    public static class RegisterRequest {
        private String name;
        private String occupation;
        private String phone;
        private String email;
        private String note;
        private Boolean isNumberDisplay;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerExpert(@RequestBody RegisterRequest req) {

        Optional<User> existingUserOpt = userRepository.findByPhone(req.getPhone());
        User user = existingUserOpt.orElseGet(User::new);

        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());

        if (req.getName()!= null) {
            String[] parts = req.getName().trim().split(" ", 2);
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1? parts[1] : "");
        }

        user.setOccupation(req.getOccupation());
        user.setNote(req.getNote());

        // FIX: Sirf tick kiya to true, warna false
        user.setIsNumberDisplay(Boolean.TRUE.equals(req.getIsNumberDisplay()));

        user.setProfileCompleted(true);
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of(
                        "message", "User registered successfully",
                        "userId", user.getId()
                )
        );
    }

    @GetMapping("/experts")
    public ResponseEntity<?> getExperts() {

        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> experts = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getOccupation() == null || user.getOccupation().isBlank()) {
                continue;
            }

            Map<String, Object> map = new HashMap<>();
            String fullName = (user.getFirstName() == null? "" : user.getFirstName()) + " " +
                              (user.getLastName() == null? "" : user.getLastName());

            map.put("id", user.getId());
            map.put("fullName", fullName.trim());
            map.put("occupation", user.getOccupation());
            map.put("email", user.getEmail());
            map.put("note", user.getNote());
            map.put("profileImageUrl", user.getProfileImageUrl());

            // FIX: null ko false mano
            Boolean isDisplay = Boolean.TRUE.equals(user.getIsNumberDisplay());
            map.put("isNumberDisplay", isDisplay);

            if (isDisplay) {
                map.put("phone", user.getPhone());
            } else {
                map.put("phone", null);
            }

            experts.add(map);
        }

        return ResponseEntity.ok(experts);
    }
}