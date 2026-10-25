// package com.smartgaon.ai.smartgaon_api.otp.controller;

// import java.util.Map;

// import com.smartgaon.ai.smartgaon_api.otp.dto.OtpSendRequest;
// import com.smartgaon.ai.smartgaon_api.otp.dto.OtpVerifyRequest;
// import com.smartgaon.ai.smartgaon_api.otp.service.OtpService;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// @RestController
// @RequestMapping("/api/otp")
// public class OtpController {

//     private final OtpService otpService;

//     public OtpController(OtpService otpService) {
//         this.otpService = otpService;
//     }

//     @PostMapping("/send")
//     public ResponseEntity<?> sendOtp(@RequestBody OtpSendRequest request) {
//         try {
//             return ResponseEntity.ok(otpService.sendOtp(request.getMobile()));
//         } catch (IllegalArgumentException ex) {
//             return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
//         } catch (IllegalStateException ex) {
//             return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
//                     .body(Map.of("success", false, "message", ex.getMessage()));
//         }
//     }

//     @PostMapping("/verify")
//     public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
//         try {
//             return ResponseEntity.ok(otpService.verifyOtp(request.getMobile(), request.getOtp()));
//         } catch (IllegalArgumentException ex) {
//             return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
//         }
//     }
// }


package com.smartgaon.ai.smartgaon_api.otp.controller;

import java.util.Map;
import java.util.Optional;
import com.smartgaon.ai.smartgaon_api.auth.service.AuthService;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.otp.dto.OtpSendRequest;
import com.smartgaon.ai.smartgaon_api.otp.dto.OtpVerifyRequest;
import com.smartgaon.ai.smartgaon_api.otp.service.OtpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;
    private final AuthService authService;
    
    private static final String TEST_PHONE = "6207720893";
    private static final String TEST_OTP = "1235";

    public OtpController(OtpService otpService, AuthService authService) {
        this.otpService = otpService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpSendRequest request) {
        try {
            if (TEST_PHONE.equals(request.getMobile())) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent (Test - 1235)",
                    "isTest", true
                ));
            }
            return ResponseEntity.ok(otpService.sendOtp(request.getMobile()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        try {
            if (TEST_PHONE.equals(request.getMobile()) && TEST_OTP.equals(request.getOtp())) {
                Optional<User> userOpt = authService.findByPhone(request.getMobile());
                if (userOpt.isPresent()) {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "verified", true,
                        "firstTime", false,
                        "message", "OTP verified successfully!",
                        "phone", request.getMobile(),
                        "user", userOpt.get()
                    ));
                } else {
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "verified", true,
                        "firstTime", true,
                        "message", "OTP verified successfully!",
                        "phone", request.getMobile()
                    ));
                }
            }
            if (TEST_PHONE.equals(request.getMobile())) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "verified", false,
                    "message", "Invalid OTP - Test OTP is 1235"
                ));
            }
            return ResponseEntity.ok(otpService.verifyOtp(request.getMobile(), request.getOtp()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}