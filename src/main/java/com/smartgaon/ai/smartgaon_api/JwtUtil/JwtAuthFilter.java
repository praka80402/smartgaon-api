package com.smartgaon.ai.smartgaon_api.JwtUtil;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.config.RedisAuthTokenService;
import com.smartgaon.ai.smartgaon_api.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final RedisAuthTokenService redisAuthTokenService;

    // FIX: only these specific /api/auth/* paths are public.
    // Previously the filter skipped EVERY path starting with "/api/auth/",
    // which meant "/api/auth/update-profile/{userId}" and
    // "/api/auth/by-pincode/{pincode}" never got their JWT read/validated,
    // so SecurityContextHolder stayed empty and Spring Security 403'd them
    // even when a valid Authorization header was sent.
    private static final List<String> PUBLIC_AUTH_PATHS = List.of(
            "/api/auth/send-otp",
            "/api/auth/signup-phone",
            "/api/auth/send-signup-otp",
            "/api/auth/verify-otp",
            "/api/auth/generate-jwt-token",
            "/api/auth/refresh",
            "/api/auth/logout"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();

        boolean isPublicAuthPath = PUBLIC_AUTH_PATHS.stream().anyMatch(path::startsWith);
        if (isPublicAuthPath) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    String tokenType = jwtUtil.extractTokenType(token);
                    if (!"REFRESH".equals(tokenType)) {
                        String userId = jwtUtil.extractUserId(token);
                        String userType = jwtUtil.extractUserType(token);

                        User user = userRepo.findById(Long.parseLong(userId)).orElse(null);
                        if (user == null) {
                            response.setStatus(403);
                            response.getWriter().write("{\"error\":\"User not found\"}");
                            return;
                        }

                        // ===== LOGOUT CHECK =====
                        boolean isValid = false;
                        if (user.getPhone() != null) {
                            String stored = redisAuthTokenService.getAccessToken(userType, user.getPhone());
                            if (token.equals(stored)) isValid = true;
                        }
                        if (!isValid && user.getEmail() != null) {
                            String stored = redisAuthTokenService.getAccessToken(userType, user.getEmail().toLowerCase());
                            if (token.equals(stored)) isValid = true;
                        }

                        if (!isValid) {
                            System.out.println("BLOCKED - Logged out token");
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Session expired or logged out\"}");
                            return;
                        }

                        String role = "ROLE_" + userType;
                        System.out.println("AUTH SET -> userId=" + userId + " role=" + role);

                        var auth = new UsernamePasswordAuthenticationToken(
                                userId, null, List.of(new SimpleGrantedAuthority(role))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                System.out.println("JWT ERROR: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
