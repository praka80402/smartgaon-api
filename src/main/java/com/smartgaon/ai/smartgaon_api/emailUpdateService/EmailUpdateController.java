package com.smartgaon.ai.smartgaon_api.emailUpdateService;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email-update")
public class EmailUpdateController {

    @Autowired
    private EmailUpdateService emailUpdateService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail");
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email is required"
            ));
        }

        String normalizedEmail = newEmail.trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return ResponseEntity.status(409).body(Map.of(
                "success", false,
                "message", "This email is already linked with our SmartGaon AI platform",
                "errorCode", "EMAIL_ALREADY_EXISTS"
            ));
        }

          emailUpdateService.sendOtp(normalizedEmail);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Verification OTP has been sent to " + newEmail
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("newEmail");
        if (email == null) email = body.get("email");
        String otp = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Email and OTP are required"
            ));
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            return ResponseEntity.status(409).body(Map.of(
                "success", false,
                "message", "This email is already linked with our SmartGaon AI platform",
                "errorCode", "EMAIL_ALREADY_EXISTS"
            ));
        }

        EmailUpdateService.OtpStatus status = emailUpdateService.verifyOtp(normalizedEmail, otp);

        return switch (status) {
            case VALID -> ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email verified successfully!",
                "verifiedEmail", normalizedEmail
            ));
            case EXPIRED -> ResponseEntity.status(410).body(Map.of(
                "success", false,
                "message", "OTP has expired. Please request a new OTP.",
                "errorCode", "OTP_EXPIRED"
            ));
            case INVALID -> ResponseEntity.status(400).body(Map.of(
                "success", false,
                "message", "Invalid OTP. Please check and try again.",
                "errorCode", "INVALID_OTP"
            ));
            default -> ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "No OTP found for this email. Please request a new OTP.",
                "errorCode", "OTP_NOT_FOUND"
            ));
        };
    }
}