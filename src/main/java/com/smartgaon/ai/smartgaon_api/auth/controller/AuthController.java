package com.smartgaon.ai.smartgaon_api.auth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.auth.service.AuthService;
import com.smartgaon.ai.smartgaon_api.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService auth;

    @Autowired
    private JwtUtil jwt;

    @Value("${google.client.native-id}")
    private String nativeClientId;

    // =====================================================
    // SIGNUP USING EMAIL + PASSWORD
    // =====================================================
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> req) {

        User user = new User();
        user.setFirstName(req.get("firstName"));
        user.setLastName(req.get("lastName"));
        user.setEmail(req.get("email"));
        user.setPhone(req.get("phone"));
        user.setPassword(req.get("password"));

        // Optional: Capture location fields if provided
        user.setState(req.get("state"));
        user.setDistrict(req.get("district"));
        user.setArea(req.get("area"));
        user.setPincode(req.get("pincode"));

        auth.signup(user);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // =====================================================
    // SIGNUP USING PHONE + OTP
    // =====================================================
    @PostMapping("/signup-phone")
    public ResponseEntity<?> signupWithPhone(@RequestParam String phone) {

        if (!phone.matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter a valid 10-digit mobile number.")
            );
        }

        Optional<User> existingUser = auth.findByPhone(phone);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "This number is already registered", "navigate", "login")
            );
        }

        return ResponseEntity.ok(auth.generateSignupOtp(phone));
    }

    // =====================================================
    // LOGIN (EMAIL + PASSWORD)
    // =====================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String password = req.get("password");

        return auth.validate(email, password)
                .map(user -> {
                    String token = jwt.generate(email);
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "user", user
                    ));
                })
                .orElseGet(() -> ResponseEntity.status(401)
                        .body(Map.of("error", "Invalid email or password")));
    }

    // =====================================================
    // GOOGLE LOGIN
    // =====================================================
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {

        String idToken = body.get("token");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(nativeClientId))
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            Optional<User> existingUser = auth.findByEmail(email);

            User user = existingUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setFirstName(name);
                newUser.setEmail(email);
                auth.saveUser(newUser);
                return newUser;
            });

            String jwtToken = jwt.generate(email);

            return ResponseEntity.ok(
                    Map.of("token", jwtToken, "email", email, "name", name, "picture", picture)
            );

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Verification failed"));
        }
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            return ResponseEntity.ok(auth.forgotPassword(email));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {

        try {
            return ResponseEntity.ok(auth.resetPassword(token, newPassword));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // OTP SEND (FOR LOGIN)
    // =====================================================
//    @PostMapping("/send-otp")
//    public ResponseEntity<?> sendOtp(@RequestParam String mobile) {
//        return auth.sendOtp(mobile);
//    }
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String mobile) {

        Optional<User> userOpt = auth.findByPhone(mobile);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 🆕 BLOCK LOGIN IF USER IS DELETED
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return ResponseEntity.status(403).body(
                        Map.of(
                                "error", "You are no longer a user.",
                                "deletedBy", user.getDeletedBy()
                        )
                );
            }
        }

        return auth.sendOtp(mobile);
    }

    // =====================================================
    // OTP VERIFY
    // =====================================================
//    @PostMapping("/verify-otp")
//    public ResponseEntity<?> verifyOtp(@RequestParam String mobile, @RequestParam String otp) {
//        return ResponseEntity.ok(auth.verifyOtp(mobile, otp));
//    }
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String mobile, @RequestParam String otp) {

        Optional<User> userOpt = auth.findByPhone(mobile);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 🆕 BLOCK LOGIN IF USER IS DELETED
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return ResponseEntity.status(403).body(
                        Map.of(
                                "error", "You are no longer a user. Deleted by: " + user.getDeletedBy(),
                                "verified", false
                        )
                );
            }
        }

        return ResponseEntity.ok(auth.verifyOtp(mobile, otp));
    }

    // =====================================================
    // GET USERS BY PINCODE
    // =====================================================
    @GetMapping("/by-pincode/{pincode}")
    public ResponseEntity<?> getUsersByPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(auth.getUsersByPinCode(pincode));
    }

    // =====================================================
    // DELETE ACCOUNT
    // =====================================================
    @DeleteMapping("/delete/{phone}")
    public ResponseEntity<?> deleteUser(@PathVariable String phone) {
        try {
            boolean deleted = auth.deleteUserByPhone(phone);

            if (!deleted) {
                return ResponseEntity.status(404).body(Map.of("error", "User not found"));
            }

            return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Something went wrong"));
        }
    }
    
 // =====================================================
 // UPDATE PROFILE
 // =====================================================
 @PutMapping("/update-profile/{id}")
 public ResponseEntity<?> updateProfile(
         @PathVariable Long id,
         @RequestBody Map<String, String> req) {

     try {
         User updatedUser = auth.updateUserProfile(id, req);
         return ResponseEntity.ok(
                 Map.of(
                         "message", "Profile updated successfully",
                         "user", updatedUser
                 )
         );
     } catch (RuntimeException e) {
         return ResponseEntity.status(400).body(
                 Map.of("error", e.getMessage())
         );
     }
 }

}
