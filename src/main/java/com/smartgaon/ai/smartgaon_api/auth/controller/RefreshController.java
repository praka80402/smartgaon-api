package com.smartgaon.ai.smartgaon_api.auth.controller;

import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.auth.dto.RefreshResult;
import com.smartgaon.ai.smartgaon_api.auth.dto.SessionInfo;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.auth.service.RefreshTokenService;
import com.smartgaon.ai.smartgaon_api.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * New endpoints for the Redis refresh-token system. Kept in a separate
 * controller so the existing AuthController is untouched.
 *
 *   POST /api/auth/refresh      -> rotate refresh token, return new access token
 *   POST /api/auth/logout       -> revoke this one session
 *   POST /api/auth/logout-all   -> revoke all sessions for the logged-in user
 *   GET  /api/auth/sessions     -> list active sessions (logged-in devices)
 *
 * Access tokens are still minted by your existing JwtUtil (HS256, subject=email).
 */
@RestController
@RequestMapping("/api/auth")
public class RefreshController {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public RefreshController(RefreshTokenService refreshTokenService,
                             JwtUtil jwtUtil,
                             UserRepository userRepository) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    // =====================================================
    // REFRESH: rotate + reuse detection, then new access token
    // =====================================================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");
        String platform = body.getOrDefault("platform", "unknown");

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "refreshToken is required"));
        }

        try {
            // Rotates the token (old one becomes invalid) and runs reuse detection.
            RefreshResult result = refreshTokenService.rotate(refreshToken, platform);

            User user = userRepository.findById(Long.valueOf(result.userId()))
                    .orElseThrow(() -> new RefreshTokenService.InvalidRefreshTokenException("User not found"));

            String role = (user.getRoles() == null || user.getRoles().isBlank()) ? "USER" : user.getRoles();
            String newAccessToken = jwtUtil.generate(user.getEmail(), role);

            return ResponseEntity.ok(Map.of(
                    "token", newAccessToken,
                    "refreshToken", result.refreshToken()
            ));

        } catch (RefreshTokenService.RefreshTokenReuseException e) {
            // Old (already-rotated) token replayed -> whole session revoked.
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Session revoked. Please log in again.",
                    "reason", "reuse_detected"
            ));
        } catch (RefreshTokenService.InvalidRefreshTokenException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    // =====================================================
    // LOGOUT: revoke just this session (client sends its refresh token)
    // =====================================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken != null && !refreshToken.isBlank()) {
            try {
                refreshTokenService.revokeByToken(refreshToken);
            } catch (Exception ignored) {
                // Already gone / malformed — logout is idempotent, treat as success.
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    // =====================================================
    // LOGOUT ALL: revoke every session for the current user
    // =====================================================
    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = currentUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        long count = refreshTokenService.revokeAll(String.valueOf(user.getId()));
        return ResponseEntity.ok(Map.of(
                "message", "Logged out from all devices",
                "sessionsRevoked", count
        ));
    }

    // =====================================================
    // SESSIONS: list active logged-in devices for the current user
    // =====================================================
    @GetMapping("/sessions")
    public ResponseEntity<?> sessions(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = currentUser(authHeader);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<SessionInfo> sessions = refreshTokenService.listSessions(String.valueOf(user.getId()));
        return ResponseEntity.ok(Map.of("sessions", sessions));
    }

    /**
     * Resolve the logged-in user from the access token.
     * Token subject is email for most users, but phone for mobile-only users,
     * so try both.
     */
    private User currentUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String subject = jwtUtil.extractEmail(authHeader.substring(7)); // email OR phone
            return userRepository.findByEmail(subject)
                    .or(() -> userRepository.findByPhone(subject))
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }
}