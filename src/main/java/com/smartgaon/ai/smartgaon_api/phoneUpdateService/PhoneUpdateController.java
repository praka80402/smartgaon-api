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
    private OtpService otpService;

    private String clean10Digit(String phone){
        if(phone == null) return "";
        String digits = phone.replaceAll("\\D", ""); 
        if(digits.startsWith("91") && digits.length() > 10){
            digits = digits.substring(2);
        }
        if(digits.startsWith("0") && digits.length() > 10){
            digits = digits.substring(1);
        }
        if(digits.length() > 10){
            digits = digits.substring(digits.length() - 10);
        }
        return digits;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {

        String phoneNumber = body.get("phoneNumber");
        String isDevStr = body.get("isDev");
        boolean isDev = "true".equalsIgnoreCase(isDevStr);

        System.out.println("Send OTP Request - phone: " + phoneNumber + " isDev: " + isDev);

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phone number is required"
            ));
        }

        String normalizedPhone = phoneNumber.trim();
        String phone10 = clean10Digit(normalizedPhone);
        if (userRepository.existsByPhone(normalizedPhone) || 
            userRepository.existsByPhone(phone10) || 
            userRepository.existsByPhone("+91"+phone10)) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "This phone number is already linked with our SmartGaon AI platform",
                    "errorCode", "PHONE_ALREADY_EXISTS"
            ));
        }

        try {
            if (isDev) {
                String devOtp = "1235";
                phoneUpdateService.saveDevOtp(normalizedPhone, devOtp);
                phoneUpdateService.saveDevOtp(phone10, devOtp);
                phoneUpdateService.saveDevOtp("+91"+phone10, devOtp);
                System.out.println("DEV MODE: OTP 1235 saved for " + normalizedPhone + " / " + phone10);

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "DEV MODE: OTP is 1235 for " + normalizedPhone
                ));
            } else {
                String otp = phoneUpdateService.sendOtp(normalizedPhone);
                otpService.sendOtpViaMsg91(normalizedPhone, otp);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Verification OTP has been sent to " + normalizedPhone
                ));
            }
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Failed to send OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {

        String phoneNumber = body.get("phoneNumber");
        if (phoneNumber == null) {
            phoneNumber = body.get("phone");
        }
        String otp = body.get("otp");
        String isDevStr = body.get("isDev");
        boolean isDev = "true".equalsIgnoreCase(isDevStr);

        System.out.println("Verify OTP Request - phone: " + phoneNumber + " otp: " + otp + " isDev: " + isDev);

        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Phone number and OTP are required"
            ));
        }

        String normalizedPhone = phoneNumber.trim();
        String phone10 = clean10Digit(normalizedPhone);

        // Check already exists
        if (userRepository.existsByPhone(normalizedPhone) || 
            userRepository.existsByPhone(phone10) || 
            userRepository.existsByPhone("+91"+phone10)) {
            return ResponseEntity.status(409).body(Map.of(
                    "success", false,
                    "message", "This phone number is already linked with our SmartGaon AI platform",
                    "errorCode", "PHONE_ALREADY_EXISTS"
            ));
        }
        if (isDev && "1235".equals(otp)) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phone number verified successfully! (DEV)",
                    "verifiedPhone", normalizedPhone
            ));
        }

        PhoneUpdateService.OtpStatus status = phoneUpdateService.verifyOtp(normalizedPhone, otp);
        
        if(status == PhoneUpdateService.OtpStatus.NOT_FOUND){
            status = phoneUpdateService.verifyOtp(phone10, otp);
        }
        if(status == PhoneUpdateService.OtpStatus.NOT_FOUND){
            status = phoneUpdateService.verifyOtp("+91"+phone10, otp);
        }

        return switch (status) {
            case VALID -> ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Phone number verified successfully!",
                    "verifiedPhone", normalizedPhone
            ));
            case EXPIRED -> ResponseEntity.status(410).body(Map.of(
                    "success", false,
                    "message", "OTP has expired. Please request a new OTP.",
                    "errorCode", "OTP_EXPIRED"
            ));
            case INVALID -> ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid OTP. Please check and try again.",
                    "errorCode", "INVALID_OTP"
            ));
            default -> ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "No OTP found for this phone number. Please request a new OTP.",
                    "errorCode", "OTP_NOT_FOUND"
            ));
        };
    }
}