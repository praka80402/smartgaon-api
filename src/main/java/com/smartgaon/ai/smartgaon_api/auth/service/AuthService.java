package com.smartgaon.ai.smartgaon_api.auth.service;

import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.JwtUtil.UserType;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.service.EmailService;
import com.smartgaon.ai.smartgaon_api.auth.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.smartgaon.ai.smartgaon_api.config.RedisAuthTokenService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final EmailService emailService;
  private final JwtUtil jwt;
    private final RedisAuthTokenService redisAuthTokenService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
     private static final long WEB_TOKEN_EXPIRY_DAYS = 1; // WEB = 1 din
    private static final long MOBILE_TOKEN_EXPIRY_DAYS = 2; // APP/MOBILE = 2 din
    private static final long REFRESH_TOKEN_EXPIRY_DAYS = 30;
 

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

        String otp = "1235";
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
 // UPDATE USER PROFILE
 // ======================================================
 public User updateUserProfile(Long id, Map<String, String> req) {

     User user = repo.findById(id)
             .orElseThrow(() -> new RuntimeException("User not found"));

     if (Boolean.TRUE.equals(user.getIsDeleted())) {
         throw new RuntimeException("User account is deleted");
     }

     // Update allowed fields only
     user.setFirstName(req.getOrDefault("firstName", user.getFirstName()));
     user.setLastName(req.getOrDefault("lastName", user.getLastName()));
     user.setPhone(req.getOrDefault("phone", user.getPhone()));
     user.setState(req.getOrDefault("state", user.getState()));
     user.setEmail(req.getOrDefault("email", user.getEmail()));
     user.setDistrict(req.getOrDefault("district", user.getDistrict()));
     user.setArea(req.getOrDefault("area", user.getArea()));
     user.setPincode(req.getOrDefault("pincode", user.getPincode()));
     user.setOccupation(req.getOrDefault("occupation", user.getOccupation()));
     user.setNote(req.getOrDefault("note", user.getNote()));
     user.setProfileImageUrl(
             req.getOrDefault("profileImageUrl", user.getProfileImageUrl())
     );
     
//     if (req.containsKey("email") && user.getEmail() == null) {
//         String email = req.get("email");
//         if (email != null && !email.isBlank()) {
//             user.setEmail(email);
//         }
//     }

     // Mark profile completed
     user.setProfileCompleted(true);

     return repo.save(user);
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
                Map.of("message", "OTP generated successfully", "phone", mobile)
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




    // ============================================================
    // GENERATE ACCESS + REFRESH TOKEN
    // ============================================================

    // public TokenResponse generateToken(String phone, String email, String userType) {
    //     if (userType == null || userType.isBlank()) {
    //         userType = "WEB";
    //     }
    //     UserType type;
    //     try {
    //         type = UserType.valueOf(userType.trim().toUpperCase());
    //     } catch (IllegalArgumentException e) {
    //         throw new IllegalArgumentException("Invalid userType. Allowed values: WEB, MOBILE");
    //     }

    //     if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
    //         throw new IllegalArgumentException("Phone or email is required");
    //     }

    //     User user;

    //     if (phone != null && !phone.isBlank()) {
    //         user = repo.findByPhone(phone.trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

    //     } else {

    //         user = repo.findByEmail(email.trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    //     }

    //     if (user.getIsDeleted() == null || user.getIsDeleted()) {
    //         throw new IllegalStateException("User account is Deleted");
    //     }
    //     long accessTokenExpiryDays = type == UserType.MOBILE ? MOBILE_TOKEN_EXPIRY_DAYS : WEB_TOKEN_EXPIRY_DAYS;
    //     long accessTokenExpirySeconds = accessTokenExpiryDays * 24L * 60L * 60L;
    //     long refreshTokenExpirySeconds = REFRESH_TOKEN_EXPIRY_DAYS * 24L * 60L * 60L;

    //     String accessToken = jwt.generateAccessToken(user, type.name(), accessTokenExpirySeconds);
    //     String refreshToken = jwt.generateRefreshToken(user, type.name(), refreshTokenExpirySeconds);
    //     return new TokenResponse(accessToken, refreshToken, "Bearer", accessTokenExpirySeconds, refreshTokenExpirySeconds);
    // }
    // public record TokenResponse(
    //         String accessToken,
    //         String refreshToken,
    //         String tokenType,
    //         long expiresIn,
    //         long refreshExpiresIn
    // ) {
    // }
    //  public TokenResponse refreshAccessToken(String refreshToken) {
    //     try {
    //         Claims claims = jwt.extractAllClaims(refreshToken);

    //         if (!"REFRESH".equals(claims.get("tokenType", String.class))) {
    //             throw new IllegalArgumentException("This is not a refresh token");
    //         }

    //         String userId = claims.getSubject();
    //         String userTypeStr = claims.get("userType", String.class);
    //         UserType type = UserType.valueOf(userTypeStr);
    //         User user = repo.findById(Long.parseLong(userId))
    //                 .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    //         if (Boolean.TRUE.equals(user.getIsDeleted())) throw new IllegalStateException("User account is deleted");
    //         if (Boolean.FALSE.equals(user.getAccountEnabled())) throw new IllegalStateException("Account disabled");
    //         long accessSec = (type == UserType.MOBILE ? MOBILE_TOKEN_EXPIRY_DAYS : WEB_TOKEN_EXPIRY_DAYS) * 86400L;
    //         long refreshSec = REFRESH_TOKEN_EXPIRY_DAYS * 86400L;
    //         String newAccessToken = jwt.generateAccessToken(user, type.name(), accessSec);
    //         String newRefreshToken = jwt.generateRefreshToken(user, type.name(), refreshSec);
    //         return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", accessSec, refreshSec);
    //     } catch (ExpiredJwtException e) {
    //         throw new IllegalStateException("Refresh token expired, please login again");
    //     } catch (Exception e) {
    //         throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
    //     }
    // }

    // public void logout(String refreshToken) {
    //     try {
    //         Claims claims = jwt.extractAllClaims(refreshToken);
    //         if (!"REFRESH".equals(claims.get("tokenType", String.class))) {
    //             throw new IllegalArgumentException("This is not a refresh token");
    //         }   
    //     } catch (ExpiredJwtException e) {
    //         return;
    //     } catch (Exception e) {
    //         throw new IllegalArgumentException("Invalid refresh token");
    //     }
    // }

   public TokenResponse generateToken(String phone, String email, String userType) {
        if (userType == null || userType.isBlank()) {
            userType = "WEB";
        }
        UserType type;
        try {
            type = UserType.valueOf(userType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userType. Allowed values: WEB, MOBILE");
        }
        if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
            throw new IllegalArgumentException("Phone or email is required");
        }

        User user;
        String loginIdentifier; // jisse login kiya usi se key banegi

        if (phone != null && !phone.isBlank()) {
            user = repo.findByPhone(phone.trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            loginIdentifier = phone.trim();
        } else {
            user = repo.findByEmail(email.trim()).orElseThrow(() -> new UsernameNotFoundException("User not found"));
            loginIdentifier = email.trim().toLowerCase();
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalStateException("User account is Deleted");
        }

        long accessTokenExpiryDays = type == UserType.MOBILE ? MOBILE_TOKEN_EXPIRY_DAYS : WEB_TOKEN_EXPIRY_DAYS;
        long accessTokenExpirySeconds = accessTokenExpiryDays * 24L * 60L * 60L;
        long refreshTokenExpirySeconds = REFRESH_TOKEN_EXPIRY_DAYS * 24L * 60L * 60L;

        String accessToken = jwt.generateAccessToken(user, type.name(), accessTokenExpirySeconds);
        String refreshToken = jwt.generateRefreshToken(user, type.name(), refreshTokenExpirySeconds);
        
        // Redis me save - ab sahi identifier se
        redisAuthTokenService.saveTokens(type.name(), loginIdentifier, accessToken, refreshToken, refreshTokenExpirySeconds);

        return new TokenResponse(accessToken, refreshToken, "Bearer", accessTokenExpirySeconds, refreshTokenExpirySeconds);
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            long refreshExpiresIn
    ) {}

     public TokenResponse refreshAccessToken(String refreshToken) {
        try {
            Claims claims = jwt.extractAllClaims(refreshToken);
            if (!"REFRESH".equals(claims.get("tokenType", String.class))) {
                throw new IllegalArgumentException("This is not a refresh token");
            }
            String userId = claims.getSubject();
            String userTypeStr = claims.get("userType", String.class);
            UserType type = UserType.valueOf(userTypeStr);
            User user = repo.findById(Long.parseLong(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            if (Boolean.TRUE.equals(user.getIsDeleted())) throw new IllegalStateException("User account is deleted");
            if (Boolean.FALSE.equals(user.getAccountEnabled())) throw new IllegalStateException("Account disabled");

            // Check karo phone ya email dono me se koi ek key exist karti hai ya nahi
            boolean exists = false;
            String actualIdentifier = null;
            if (user.getPhone() != null && redisAuthTokenService.isTokenExists(type.name(), user.getPhone())) {
                exists = true;
                actualIdentifier = user.getPhone();
            } else if (user.getEmail() != null && redisAuthTokenService.isTokenExists(type.name(), user.getEmail())) {
                exists = true;
                actualIdentifier = user.getEmail().toLowerCase();
            }
            if (!exists) throw new IllegalStateException("Session expired or logged out, please login again");

            long accessSec = (type == UserType.MOBILE ? MOBILE_TOKEN_EXPIRY_DAYS : WEB_TOKEN_EXPIRY_DAYS) * 86400L;
            long refreshSec = REFRESH_TOKEN_EXPIRY_DAYS * 86400L;
            String newAccessToken = jwt.generateAccessToken(user, type.name(), accessSec);
            String newRefreshToken = jwt.generateRefreshToken(user, type.name(), refreshSec);

            redisAuthTokenService.saveTokens(type.name(), actualIdentifier, newAccessToken, newRefreshToken, refreshSec);

            return new TokenResponse(newAccessToken, newRefreshToken, "Bearer", accessSec, refreshSec);
        } catch (ExpiredJwtException e) {
            throw new IllegalStateException("Refresh token expired, please login again");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
        }
    }

public boolean secureLogout(String accessToken, Map<String, String> body) {
    try {
        Claims claims = jwt.extractAllClaims(accessToken);
        String tokenUserId = claims.getSubject();
        String userType = claims.get("userType", String.class);

        User user = repo.findById(Long.parseLong(tokenUserId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String reqUserId = body.get("userId");
        String reqEmail = body.get("email");
        String reqPhone = body.get("phone");

        // 1. CHECK: Body ka userId aur Token ka userId same hona chahiye
        if (reqUserId != null && !tokenUserId.equals(reqUserId.trim())) {
            throw new IllegalArgumentException("Token and userId mismatch");
        }

        // 2. CHECK: Jo email/phone body me bheja hai wo isi user ka hai ya nahi
        if (reqEmail != null && !reqEmail.isBlank()) {
            if (user.getEmail() == null || !user.getEmail().equalsIgnoreCase(reqEmail.trim())) {
                throw new IllegalArgumentException("Email does not belong to this token user");
            }
        }
        if (reqPhone != null && !reqPhone.isBlank()) {
            if (user.getPhone() == null || !user.getPhone().equals(reqPhone.trim())) {
                throw new IllegalArgumentException("Phone does not belong to this token user");
            }
        }

        // 3. DELETE: Token usi user ka hai ye confirm hai, ab delete karo
        boolean del1 = false, del2 = false;
        if (user.getPhone() != null) del1 = redisAuthTokenService.deleteToken(userType, user.getPhone());
        if (user.getEmail() != null) del2 = redisAuthTokenService.deleteToken(userType, user.getEmail().toLowerCase());

        System.out.println("Secure Logout: userId=" + tokenUserId + " phone=" + del1 + " email=" + del2);
        return del1 || del2;

    } catch (ExpiredJwtException e) {
        return true;
    } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage());
    }
}
}