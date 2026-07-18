package com.smartgaon.ai.smartgaon_api.otp.controller;

import java.util.Map;

import com.smartgaon.ai.smartgaon_api.otp.dto.OtpSendRequest;
import com.smartgaon.ai.smartgaon_api.otp.dto.OtpVerifyRequest;
import com.smartgaon.ai.smartgaon_api.otp.service.OtpService;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.User;

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
    private final JwtUtil jwt;

    public OtpController(OtpService otpService, JwtUtil jwt) {
        this.otpService = otpService;
        this.jwt = jwt;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendOtp(@RequestBody OtpSendRequest request) {
        try {
            return ResponseEntity.ok(otpService.sendOtp(request.getMobile()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest request) {
        try {
            Map<String, Object> result = otpService.verifyOtp(request.getMobile(), request.getOtp());
            if (Boolean.TRUE.equals(result.get("verified"))) {
                User user = (User) result.get("user");
                if (user != null) {
                    String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
                    String subject = (user.getEmail() != null && !user.getEmail().isBlank()) ? user.getEmail() : user.getPhone();
                    String token = jwt.generate(subject, role);
                    result.put("token", token);
                }
            }
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
