package com.smartgaon.ai.smartgaon_api.auth.service;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    // ======================================================
    // SIGNUP
    // ======================================================
    public User signup(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }


    // ======================================================
    // SIGNUP OTP GENERATION
    // ======================================================
    public Map<String, Object> generateSignupOtp(String phone) {

        String otp = "1235"; // FIXED OTP
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        // Create new user only for OTP stage
        User user = new User();
        user.setPhone(phone);
        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        user.setVerified(false);

        repo.save(user);

        return Map.of(
                "otp", otp,
                "message", "Signup OTP generated successfully",
                "phone", phone
        );
    }


    // ======================================================
    // SEND OTP FOR LOGIN
    // ======================================================
//    public ResponseEntity<?> sendOtp(String mobile) {
//
//        if (!mobile.matches("^[6-9]\\d{9}$")) {
//            return ResponseEntity.badRequest().body(
//                    Map.of("error", "Please enter a valid 10-digit mobile number.")
//            );
//        }
//
//        Optional<User> existingUser = repo.findByPhone(mobile);
//
//        if (existingUser.isEmpty()) {
//            return ResponseEntity.status(404).body(
//                    Map.of("error", "User not found", "navigate", "signup")
//            );
//        }
//
//        User user = existingUser.get();
//        
//        if (user.isDeleted()) {
//            return ResponseEntity.status(403).body(
//                    Map.of("error", "You are no longer a user.", "deletedBy", user.getDeletedBy())
//            );
//        }
//
//        String otp = "1235";
//        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
//
//        user.setOtp(otp);
//        user.setOtpExpiry(expiry);
//        user.setVerified(false);
//
//        repo.save(user);
//
//        return ResponseEntity.ok(
//                Map.of("otp", otp, "message", "OTP generated successfully")
//        );
//    }

    public ResponseEntity<?> sendOtp(String mobile) {

        if (!mobile.matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter a valid 10-digit mobile number.")
            );
        }

        Optional<User> existingUser = repo.findByPhone(mobile);

        if (existingUser.isEmpty()) {
            return ResponseEntity.status(404).body(
                    Map.of("error", "User not found", "navigate", "signup")
            );
        }

        User user = existingUser.get();
        
        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            return ResponseEntity.status(403).body(
                    Map.of("error", "Your account has been disabled by admin.")
            );
        }


        // 🆕 BLOCK LOGIN IF ACCOUNT IS DELETED
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            return ResponseEntity.status(403).body(
                    Map.of(
                            "error", "You are no longer a user.",
                            "deletedBy", user.getDeletedBy()
                    )
            );
        }

        // Generate OTP
        String otp = "1235";
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        user.setVerified(false);

        repo.save(user);

        return ResponseEntity.ok(
                Map.of("otp", otp, "message", "OTP generated successfully")
        );
    }

    // ======================================================
    // VERIFY OTP
    // ======================================================
    public Map<String, Object> verifyOtp(String mobile, String otp) {

        Optional<User> userOpt = repo.findByPhone(mobile);
        Map<String, Object> response = new HashMap<>();

        if (userOpt.isEmpty()) {
            response.put("error", "Mobile number not found");
            return response;
        }
        

        User user = userOpt.get();

        // Handle null expiry (safety)
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            response.put("error", "OTP expired. Please resend.");
            response.put("verified", false);
            return response;
        }
        
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            response.put("error", "You are no longer a user. Deleted by: " + user.getDeletedBy());
            response.put("verified", false);
            return response;
        }
        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            response.put("error", "Your account has been disabled by admin.");
            response.put("verified", false);
            return response;
        }

        

        if (!otp.equals(user.getOtp())) {
            response.put("message", "Wrong OTP");
            response.put("verified", false);
            return response;
        }

        user.setVerified(true);
        repo.save(user);

        response.put("message", "OTP verified successfully!");
        response.put("phone", user.getPhone());
        response.put("verified", true);
        response.put("firstTime", !user.isProfileCompleted());
        response.put("user", user);

        return response;
    }
    


    // ======================================================
    // EMAIL + PASSWORD LOGIN
    // ======================================================
    public Optional<User> validate(String email, String pass) {
        return repo.findByEmail(email)
                .filter(u -> !Boolean.TRUE.equals(u.getIsDeleted()))
                .filter(u -> Boolean.TRUE.equals(u.getAccountEnabled()))
                .filter(u -> encoder.matches(pass, u.getPassword()));
    }


    // ======================================================
    // FORGOT & RESET PASSWORD
    // ======================================================
    public String forgotPassword(String email) {
        Optional<User> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) return "User not found";

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        repo.save(user);

        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendResetEmail(email, resetUrl);

        return "Password reset link sent to your email.";
    }

    public String resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = repo.findByResetToken(token);
        if (optionalUser.isEmpty()) return "Invalid or expired token.";

        User user = optionalUser.get();
        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);

        repo.save(user);

        return "Password reset successful.";
    }


    // ======================================================
    // HELPERS
    // ======================================================
    public Optional<User> findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public User saveUser(User user) {
        return repo.save(user);
    }

    public List<User> getUsersByPinCode(String pincode) {
        return repo.findByPincode(pincode);
    }

    public boolean deleteUserByPhone(String phone) {
        Optional<User> userOpt = repo.findByPhone(phone);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // 🆕 Soft Delete
            user.setIsDeleted(true);
            user.setDeletedBy("USER");   // because user deletes own account
            user.setDeletedAt(LocalDateTime.now());
            user.setVerified(false);

            repo.save(user);
            return true;
        }

        return false;
    }
    public List<User> getAllUsers() {
        return repo.findAll();
    }

}
