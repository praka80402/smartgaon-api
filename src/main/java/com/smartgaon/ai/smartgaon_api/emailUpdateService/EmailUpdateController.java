package com.smartgaon.ai.smartgaon_api.emailUpdateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email-update")
public class EmailUpdateController {

    @Autowired
    private EmailUpdateService emailUpdateService;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String newEmail = body.get("newEmail");
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email is required"));
        }
        String otp = emailUpdateService.sendOtp(newEmail);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Verification OTP has been sent to " + newEmail,
            "otp", otp
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("newEmail");
        String otp = body.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email and OTP are required"));
        }

        EmailUpdateService.OtpStatus status = emailUpdateService.verifyOtp(email, otp);

        switch (status) {
            case VALID:
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Email verified successfully!",
                    "verifiedEmail", email
                ));
            case EXPIRED:
                return ResponseEntity.status(410).body(Map.of(
                    "success", false,
                    "message", "OTP has expired. Please request a new OTP.",
                    "errorCode", "OTP_EXPIRED"
                ));
            case INVALID:
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "Invalid OTP. Please check and try again.",
                    "errorCode", "INVALID_OTP"
                ));
            case NOT_FOUND:
            default:
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "No OTP found for this email. Please request a new OTP.",
                    "errorCode", "OTP_NOT_FOUND"
                ));
        }
    }
}