//package com.smartgaon.ai.smartgaon_api.controller;
//
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.service.AuthService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    @Autowired
//    private AuthService authService;
//
//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody Map<String, String> loginData) {
//        String email = loginData.get("email");
//        String password = loginData.get("password");
//        Optional<User> userOpt = authService.validateUser(email, password);
//
//        if (userOpt.isPresent()) {
//            return ResponseEntity.ok("Login successful");
//        } else {
//            return ResponseEntity.status(401).body("Invalid email or password");
//        }
//    }
//}

package com.smartgaon.ai.smartgaon_api.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.service.AuthService;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    @Autowired 
    private AuthService auth;
    @Autowired 
    private JwtUtil jwt;
    
    @Value("${google.client.id}")
    private String googleClientId;

    @PostMapping("/signup")
    
    public ResponseEntity<String> signup(@RequestBody Map<String, String> req) {
        User user = new User();
        user.setFirstName(req.get("firstName"));
        user.setLastName(req.get("lastName"));
        user.setEmail(req.get("email"));
        user.setPhone(req.get("phone"));
        user.setPassword(req.get("password"));

        auth.signup(user);
        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        return auth.validate(email, password)
                   .map(u -> ResponseEntity.ok(jwt.generate(email)))  
                   .orElseGet(() -> ResponseEntity.status(401).body("Invalid email or password"));
    }
    
    
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("token");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            Optional<User> existingUser = auth.findByEmail(email);
            User user;
            if (existingUser.isEmpty()) {
                user = new User();
                user.setFirstName(name);
                user.setEmail(email);
                user.setPassword(null);
                user.setProfileImage(null);
                auth.saveUser(user);
            } else {
                user = existingUser.get();
            }

            String jwtToken = jwt.generate(email);
            return ResponseEntity.ok(Map.of("token", jwtToken, "email", email, "name", name, "picture", picture));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Verification failed"));
        }
    }
    
    //  Forgot Password API
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            String message = auth.forgotPassword(email);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //  Reset Password API
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {
        try {
            String message = auth.resetPassword(token, newPassword);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    
    
    
    
}