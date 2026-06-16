package com.smartgaon.ai.smartgaon_api.otp.service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final String REDIS_KEY_PREFIX = "otp:msg91:";
    private static final String SIGNUP_REDIS_KEY_PREFIX = "otp:signup:msg91:";

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    @Value("${msg91.base-url:https://control.msg91.com/api/v5/flow}")
    private String msg91BaseUrl;

    @Value("${msg91.authkey:}")
    private String msg91AuthKey;

    @Value("${msg91.template-id:}")
    private String msg91TemplateId;

    @Value("${msg91.otp-ttl-minutes:5}")
    private long otpTtlMinutes;

    public OtpService(RestTemplate restTemplate, StringRedisTemplate redisTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
    }

    public Map<String, Object> sendOtp(String mobile) {
        if (mobile == null || !mobile.matches("^[6-9]\\d{9}$")) {
            throw new IllegalArgumentException("Please enter a valid 10-digit mobile number.");
        }

        Optional<User> existingUser = userRepository.findByPhone(mobile);

        if (existingUser.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            response.put("navigate", "signup");
            return response;
        }

        User user = existingUser.get();

        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", "Your account has been disabled by admin.");
            return response;
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("message", "You are no longer a user.");
            response.put("deletedBy", user.getDeletedBy());
            return response;
        }

        String normalizedMobile = normalizeMobile(mobile);
        String otp = generateOtp();

        return sendOtpInternal(normalizedMobile, otp, redisKey(normalizedMobile), "OTP sent successfully");
    }

    public Map<String, Object> sendSignupOtp(String mobile) {
        String normalizedMobile = normalizeMobile(mobile);
        String otp = generateOtp();
        String plainMobile = stripCountryCode(normalizedMobile);

        User user = new User();
        user.setPhone(plainMobile);
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(10));
        user.setVerified(false);
        userRepository.save(user);

        return sendOtpInternal(
                normalizedMobile,
                otp,
                signupRedisKey(normalizedMobile),
                "Signup OTP sent successfully"
        );
    }

    public Map<String, Object> verifyOtp(String mobile, String otp) {
        String normalizedMobile = normalizeMobile(mobile);
        String loginKey = redisKey(normalizedMobile);
        String signupKey = signupRedisKey(normalizedMobile);
        String loginStoredOtp = redisTemplate.opsForValue().get(loginKey);
        String signupStoredOtp = redisTemplate.opsForValue().get(signupKey);
        String phone = stripCountryCode(normalizedMobile);
        if (otp.equals(loginStoredOtp)) {
            redisTemplate.delete(loginKey);
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setVerified(true);
                userRepository.save(user);

                return buildVerifiedResponse(user.getPhone(), true, !user.isProfileCompleted(), user, "OTP verified successfully!");
            }

            return buildVerifiedResponse(phone, true, false, null, "OTP verified successfully!");
        }

        if (otp.equals(signupStoredOtp)) {
            redisTemplate.delete(signupKey);
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setVerified(true);
                userRepository.save(user);

                return buildVerifiedResponse(user.getPhone(), true, !user.isProfileCompleted(), user, "OTP verified successfully!");
            }

            return buildVerifiedResponse(phone, true, false, null, "OTP verified successfully!");
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", false);
        response.put("verified", false);

        if (loginStoredOtp != null || signupStoredOtp != null) {
            response.put("message", "Invalid OTP");
        } else {
            response.put("message", "OTP expired or not found. Please resend.");
        }
        return response;
    }

    private void sendOtpViaMsg91(String normalizedMobile, String otp) {
        validateGatewayConfig();

        String url = UriComponentsBuilder
                .fromHttpUrl(msg91BaseUrl)
                .queryParam("authkey", msg91AuthKey)
                .queryParam("accept", MediaType.APPLICATION_JSON_VALUE)
                .queryParam("content-type", MediaType.APPLICATION_JSON_VALUE)
                .toUriString();

        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("mobiles", normalizedMobile);
        recipient.put("var", otp);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("template_id", msg91TemplateId);
        body.put("recipients", new Object[] { recipient });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            log.info("Msg91 OTP response status={} body={}", response.getStatusCode(), response.getBody());
        } catch (HttpStatusCodeException ex) {
            log.error("Msg91 OTP request failed: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Failed to send OTP via Msg91");
        }
    }

    private void validateGatewayConfig() {
        if (msg91AuthKey == null || msg91AuthKey.isBlank()) {
            throw new IllegalStateException("Msg91 auth key is not configured");
        }
        if (msg91TemplateId == null || msg91TemplateId.isBlank()) {
            throw new IllegalStateException("Msg91 template id is not configured");
        }
    }

    private String normalizeMobile(String mobile) {
        if (mobile == null) {
            throw new IllegalArgumentException("Mobile number is required");
        }

        String trimmed = mobile.trim();
        if (trimmed.matches("^91[6-9]\\d{9}$")) {
            return trimmed;
        }
        if (trimmed.matches("^[6-9]\\d{9}$")) {
            return "91" + trimmed;
        }

        throw new IllegalArgumentException("Please enter a valid 10-digit mobile number");
    }

    private String stripCountryCode(String mobile) {
        if (mobile != null && mobile.startsWith("91") && mobile.length() == 12) {
            return mobile.substring(2);
        }
        return mobile;
    }

    private Map<String, Object> buildVerifiedResponse(
            String phone,
            boolean verified,
            boolean firstTime,
            User user,
            String message
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", message);
        response.put("phone", phone);
        response.put("verified", verified);
        response.put("firstTime", firstTime);
        response.put("user", user);
        return response;
    }

    private String generateOtp() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    private String redisKey(String normalizedMobile) {
        return REDIS_KEY_PREFIX + normalizedMobile;
    }

    private String signupRedisKey(String normalizedMobile) {
        return SIGNUP_REDIS_KEY_PREFIX + normalizedMobile;
    }

    private Map<String, Object> sendOtpInternal(String normalizedMobile, String otp, String redisKey, String message) {
        sendOtpViaMsg91(normalizedMobile, otp);
        redisTemplate.opsForValue().set(redisKey, otp, Duration.ofMinutes(otpTtlMinutes));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("mobile", normalizedMobile);
        response.put("expiresInMinutes", otpTtlMinutes);
        return response;
    }
}
