package com.smartgaon.ai.smartgaon_api.phoneUpdateService;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;

import com.smartgaon.ai.smartgaon_api.otp.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/phone-update")
public class PhoneUpdateController {

    @Autowired
    private PhoneUpdateService phoneUpdateService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService ;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(
            @RequestBody Map<String, String> body) {

        String phoneNumber = body.get("phoneNumber");

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phone number is required"
            ));
        }

        String normalizedPhone = phoneNumber.trim();

        // Check phone already exists
        if (userRepository.existsByPhone(normalizedPhone)) {

            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message",
                    "This phone number is already linked with our SmartGaon AI platform",
                    "errorCode",
                    "PHONE_ALREADY_EXISTS"
            ));
        }

       String otp = phoneUpdateService.sendOtp(normalizedPhone);
        otpService.sendOtpViaMsg91(normalizedPhone, otp);
      return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "Verification OTP has been sent to " + normalizedPhone

));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody Map<String, String> body) {

        String phoneNumber = body.get("phoneNumber");

        if (phoneNumber == null) {
            phoneNumber = body.get("phone");
        }

        String otp = body.get("otp");

        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message",
                    "Phone number and OTP are required"
            ));
        }

        String normalizedPhone = phoneNumber.trim();

        // Check phone already exists
        if (userRepository.existsByPhone(normalizedPhone)) {

            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message",
                    "This phone number is already linked with our SmartGaon AI platform",
                    "errorCode",
                    "PHONE_ALREADY_EXISTS"
            ));
        }

        PhoneUpdateService.OtpStatus status =
                phoneUpdateService.verifyOtp(
                        normalizedPhone,
                        otp
                );

        return switch (status) {

            case VALID -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "message",
                    "Phone number verified successfully!",
                    "verifiedPhone",
                    normalizedPhone
            ));

            case EXPIRED -> ResponseEntity.status(410).body(Map.of(
                    "success", false,
                    "message",
                    "OTP has expired. Please request a new OTP.",
                    "errorCode",
                    "OTP_EXPIRED"
            ));

            case INVALID -> ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message",
                    "Invalid OTP. Please check and try again.",
                    "errorCode",
                    "INVALID_OTP"
            ));

            default -> ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message",
                    "No OTP found for this phone number. Please request a new OTP.",
                    "errorCode",
                    "OTP_NOT_FOUND"
            ));
        };
    }
}