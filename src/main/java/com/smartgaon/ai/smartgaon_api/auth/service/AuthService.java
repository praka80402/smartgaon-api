//
//
//package com.smartgaon.ai.smartgaon_api.service;
//
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Service;
//import java.util.UUID;
//import java.util.Optional;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository repo;
//    
//    @Autowired
//    private EmailService emailService;
//
//
//    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
//
//    public User signup(User u) {
//        u.setPassword(encoder.encode(u.getPassword()));
//        return repo.save(u);
//    }
//
//    public Optional<User> validate(String email, String pass) {
//        return repo.findByEmail(email)
//                   .filter(u -> encoder.matches(pass, u.getPassword()));
//    }
//    
//    public String forgotPassword(String email) {
//        Optional<User> optionalUser = repo.findByEmail(email);
//        if (optionalUser.isEmpty()) {
//            return "User not found";
//        }
//
//        User user = optionalUser.get();
//        String token = UUID.randomUUID().toString();
//        user.setResetToken(token);
//        repo.save(user);
//
//        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
//        emailService.sendResetEmail(user.getEmail(), resetUrl);
//
//        return "Password reset link sent to your email.";
//    }
//
//    // Reset Password
//    public String resetPassword(String token, String newPassword) {
//        Optional<User> optionalUser = repo.findByResetToken(token);
//        if (optionalUser.isEmpty()) {
//            return "Invalid or expired token.";
//        }
//
//        User user = optionalUser.get();
//        user.setPassword(encoder.encode(newPassword));
//        user.setResetToken(null);
//        repo.save(user);
//
//        return "Password reset successful.";
//    }
//    
//    
//    public Optional<User> findByEmail(String email) {
//        return repo.findByEmail(email);
//    }
//
//   
//    public User saveUser(User user) {
//        return repo.save(user);
//    }
//}

//------------------------------------------------------------------------------

package com.smartgaon.ai.smartgaon_api.auth.service;



import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.service.EmailService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

	@Autowired
    private final UserRepository repo;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // -------------------- SIGNUP --------------------
    public User signup(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }
    
    public Map<String, Object> generateSignupOtp(String phone) {

        String otp = "334423";  // default otp
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        User user = new User();
        user.setPhone(phone);
        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        user.setVerified(false);

        repo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("otp", otp);
        response.put("message", "OTP generated successfully for signup");
        response.put("phone", phone);

        return response;
    }

    public Optional<User> findByPhone(String phone) {
        return repo.findByPhone(phone);
    }

    // -------------------- LOGIN VALIDATION --------------------
    public Optional<User> validate(String email, String pass) {
        return repo.findByEmail(email)
                .filter(u -> encoder.matches(pass, u.getPassword()));
    }

    // -------------------- FORGOT PASSWORD --------------------
    public String forgotPassword(String email) {
        Optional<User> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) return "User not found";

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repo.save(user);

        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendResetEmail(user.getEmail(), resetUrl);

        return "Password reset link sent to your email.";
    }

    // -------------------- RESET PASSWORD --------------------
    public String resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = repo.findByResetToken(token);
        if (optionalUser.isEmpty()) return "Invalid or expired token.";

        User user = optionalUser.get();
        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);
        repo.save(user);

        return "Password reset successful.";
    }

    // -------------------- SEND OTP (DEFAULT FIXED OTP) --------------------

    public ResponseEntity<?> sendOtp(String mobile) {

        if (!mobile.matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter a valid 10-digit mobile number.")
            );
        }
    	
        String otp = "123500";  // FIXED DEFAULT 6 DIGIT OTP
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

        Optional<User> existingUser = repo.findByPhone(mobile);
        User user = existingUser.orElse(new User());

        user.setPhone(mobile);
        user.setOtp(otp);
        user.setOtpExpiry(expiry);
        user.setVerified(false);

        repo.save(user);
        
        return ResponseEntity.ok(
                Map.of(
                    "otp", otp,
                    "message", "OTP generated successfully"
                )
        );

//        return "OTP generated successfully (default).";
    }

    // -------------------- VERIFY OTP --------------------
    public Map<String, Object> verifyOtp(String mobile, String otp) {

        Optional<User> userOpt = repo.findByPhone(mobile);
        Map<String, Object> response = new HashMap<>();

        if (userOpt.isEmpty()) {
            response.put("error", "Mobile number not found.");
            return response;
        }

        User user = userOpt.get();

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            response.put("error", "OTP expired. Please resend.");
            return response;
        }

        if (!user.getOtp().equals(otp)) {
            response.put("error", "OTP invalid. Please try again.");
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

    // -------------------- HELPERS --------------------
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    public User saveUser(User user) {
        return repo.save(user);
    }
    
    public List<User> getUsersByPinCode(String pincode) {
        return repo.findByPincode(pincode);
    }
}



