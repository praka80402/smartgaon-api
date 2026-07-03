package com.smartgaon.ai.smartgaon_api.JwtUtil;

import com.smartgaon.ai.smartgaon_api.auth.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;   // NEW: to check Redis sessions

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // Always clear context first
        SecurityContextHolder.clearContext();

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);
                String userId = jwtUtil.extractUserId(token);      // uid claim (may be null on legacy tokens)
                String sessionId = jwtUtil.extractSessionId(token); // sid claim (may be null on legacy tokens)

                // =========================================================
                // SESSION REVOCATION CHECK
                // If the token carries a session (uid + sid), the matching
                // refresh:{uid}:{sid} key MUST still exist in Redis. logout
                // and logout-all delete that key, so a logged-out access
                // token fails here and is treated as unauthenticated —
                // protected APIs (job section, etc.) stop working immediately.
                // =========================================================
                boolean sessionOk;
                if (userId != null && sessionId != null) {
                    sessionOk = refreshTokenService.sessionExists(userId, sessionId);
                } else {
                    // Legacy token minted before this change had no sid.
                    // Reject it so it can't outlive a logout. Users just log in
                    // again to get a session-bound token.
                    sessionOk = false;
                }

                if (sessionOk) {
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
                // else: leave context cleared -> request proceeds as anonymous ->
                // Spring Security returns 403 for protected endpoints.

            } catch (Exception e) {
                // Invalid / expired / tampered token -> stay unauthenticated.
            }
        }

        chain.doFilter(request, response);
    }
}