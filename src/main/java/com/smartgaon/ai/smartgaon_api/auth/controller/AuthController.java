package com.smartgaon.ai.smartgaon_api.auth.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.auth.service.AuthService;
import com.smartgaon.ai.smartgaon_api.auth.service.RefreshTokenService;   // NEW
import com.smartgaon.ai.smartgaon_api.auth.dto.RefreshResult;             // NEW
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.otp.service.OtpService;
import com.google.firebase.auth.FirebaseAuth;              // NEW (Firebase login)
import com.google.firebase.auth.FirebaseToken;             // NEW (Firebase login)

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

    @Autowired
    private OtpService otpService;

    @Autowired
    private RefreshTokenService refreshTokenService;   // NEW

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
    // SEND OTP FOR SIGNUP
    // =====================================================
    @PostMapping("/send-signup-otp")
    public ResponseEntity<?> sendSignupOtp(@RequestParam String mobile) {

        if (!mobile.matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter a valid 10-digit mobile number.")
            );
        }

        Optional<User> existingUser = auth.findByPhone(mobile);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body(
                    Map.of("error", "This number is already registered", "navigate", "login")
            );
        }

        return ResponseEntity.ok(otpService.sendSignupOtp(mobile));
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
                    String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
                    RefreshResult rt = refreshTokenService.issue(
                            String.valueOf(user.getId()),
                            req.getOrDefault("platform", "unknown"));
                    String token = jwt.generate(email, role, rt.userId(), rt.sessionId());
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "refreshToken", rt.refreshToken(),
                            "user", user));
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

            String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
            RefreshResult rt = refreshTokenService.issue(
                    String.valueOf(user.getId()),
                    body.getOrDefault("platform", "unknown"));
            String jwtToken = jwt.generate(email, role, rt.userId(), rt.sessionId());

            return ResponseEntity.ok(
                    Map.of("token", jwtToken, "refreshToken", rt.refreshToken(),
                           "email", email, "name", name, "picture", picture)
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
    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String mobile) {

        Optional<User> userOpt = auth.findByPhone(mobile);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // BLOCK LOGIN IF USER IS DELETED
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
    // OTP VERIFY  (mobile OTP login -> issues access + refresh tokens)
    // =====================================================
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String mobile, @RequestParam String otp) {

        Optional<User> userOpt = auth.findByPhone(mobile);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // BLOCK LOGIN IF USER IS DELETED
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return ResponseEntity.status(403).body(
                        Map.of(
                                "error", "You are no longer a user. Deleted by: " + user.getDeletedBy(),
                                "verified", false
                        )
                );
            }
        }

        Map<String, Object> result = auth.verifyOtp(mobile, otp);

        // On successful OTP verification, issue access + refresh tokens.
        if (Boolean.TRUE.equals(result.get("verified"))) {
            auth.findByPhone(mobile).ifPresent(user -> {
                String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
                // Mobile-only users may have no email; fall back to phone as the token subject.
                String subject = (user.getEmail() != null && !user.getEmail().isBlank())
                        ? user.getEmail() : user.getPhone();
                RefreshResult rt = refreshTokenService.issue(String.valueOf(user.getId()), "unknown");
                result.put("token", jwt.generate(subject, role, rt.userId(), rt.sessionId()));
                result.put("refreshToken", rt.refreshToken());
            });
        }

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // FIREBASE LOGIN (website Google login)
    // Website signs in with Firebase, then sends the Firebase ID token here.
    // We verify it, find-or-create the user, and issue our own JWT + refresh
    // token (Redis-backed) exactly like the other login flows.
    // =====================================================
    @PostMapping("/firebase")
    public ResponseEntity<?> firebaseLogin(@RequestBody Map<String, String> body) {

        String firebaseToken = body.get("token");
        if (firebaseToken == null || firebaseToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing token"));
        }

        FirebaseToken decoded;
        try {
            // Verifies signature + expiry against Firebase. Throws if invalid.
            decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Firebase token"));
        }

        String email = decoded.getEmail();
        String uid = decoded.getUid();
        String name = decoded.getName();
        String picture = decoded.getPicture();

        if (email == null || email.isBlank()) {
            return ResponseEntity.status(400).body(Map.of("error", "Firebase token has no email"));
        }

        // Find existing user by email, else create one (auto sign-up).
        User user = auth.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFirstName(name);
            u.setFirebaseUid(uid);
            u.setAuthProvider("GOOGLE");
            u.setVerified(true);
            auth.saveUser(u);
            return u;
        });

        // Block deleted / disabled accounts, same as other login flows.
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "You are no longer a user.",
                    "deletedBy", user.getDeletedBy()));
        }
        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Your account has been disabled by admin."));
        }

        // Keep firebaseUid up to date for existing users who logged in before.
        if (user.getFirebaseUid() == null || user.getFirebaseUid().isBlank()) {
            user.setFirebaseUid(uid);
            auth.saveUser(user);
        }

        String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
        RefreshResult rt = refreshTokenService.issue(
                String.valueOf(user.getId()),
                body.getOrDefault("platform", "web"));
        String token = jwt.generate(email, role, rt.userId(), rt.sessionId());

        return ResponseEntity.ok(Map.of(
                "token", token,
                "refreshToken", rt.refreshToken(),
                "email", email,
                "name", name == null ? "" : name,
                "picture", picture == null ? "" : picture,
                "user", user));
    }

    // =====================================================
    // GET USERS BY PINCODE
    // =====================================================
    @GetMapping("/by-pincode/{pincode}")
    public ResponseEntity<?> getUsersByPincode(@PathVariable String pincode) {
        return ResponseEntity.ok(auth.getUsersByPinCode(pincode));
    }

    // =====================================================
    // CHECK ACCOUNT STATUS (ACTIVE / DISABLED / DELETED)
    // =====================================================
    @PostMapping("/account-status")
    public ResponseEntity<?> checkAccountStatus(@RequestBody Map<String, String> req) {

        String phone = req.get("phone");
        String email = req.get("email");

        Optional<User> userOpt = Optional.empty();

        if (phone != null && !phone.isBlank()) {
            userOpt = auth.findByPhone(phone);
        }
        else if (email != null && !email.isBlank()) {
            userOpt = auth.findByEmail(email);
        }

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(
                    Map.of(
                            "exists", false,
                            "status", "NOT_FOUND",
                            "allowed", false
                    )
            );
        }

        User user = userOpt.get();

        // DELETED
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            return ResponseEntity.ok(
                    Map.of(
                            "exists", true,
                            "status", "DELETED",
                            "allowed", false,
                            "deletedBy", user.getDeletedBy()
                    )
            );
        }

        // DISABLED (if you have this flag)
        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            return ResponseEntity.ok(
                    Map.of(
                            "exists", true,
                            "status", "DISABLED",
                            "allowed", false,
                            "reason", "Account disabled by admin"
                    )
            );
        }

        // ACTIVE
        return ResponseEntity.ok(
                Map.of(
                        "exists", true,
                        "status", "ACTIVE",
                        "allowed", true
                )
        );
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