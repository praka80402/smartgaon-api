package com.smartgaon.ai.smartgaon_api.controller;

import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

            if (user.isEmpty()) return ResponseEntity.status(404).body("User not found");

            user.get().setPassword(null);
            return ResponseEntity.ok(user.get());
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid token");
        }
    }
}

