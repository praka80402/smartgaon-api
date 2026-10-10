package com.smartgaon.ai.smartgaon_api.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        long start = System.currentTimeMillis();

        // 🔍 Extract User Identity & Phone from JWT Token or Request Params
        String userId = "GUEST";
        String phone = "N/A";

        // 1. Try reading from Authorization Header (JWT Token)
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                if (claims != null) {
                    if (claims.getSubject() != null) {
                        userId = claims.getSubject();
                    } else if (claims.get("userId") != null) {
                        userId = String.valueOf(claims.get("userId"));
                    }
                    if (claims.get("phone") != null) {
                        phone = String.valueOf(claims.get("phone"));
                    }
                }
            } catch (Exception ignored) {
                // Token parse fallback
            }
        }

        // 2. Fallback: Try reading phone/mobile from Request Parameters (e.g. login/send-otp)
        if ("N/A".equals(phone)) {
            String paramPhone = req.getParameter("mobile");
            if (paramPhone == null || paramPhone.isBlank()) {
                paramPhone = req.getParameter("phone");
            }
            if (paramPhone != null && !paramPhone.isBlank()) {
                phone = paramPhone;
            }
        }

        String clientIp = req.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = req.getRemoteAddr();
        }

        log.info("Incoming request: {} {} | UserID: {} | Phone: {} | IP: {}",
                req.getMethod(), req.getRequestURI(), userId, phone, clientIp);

        try {
            chain.doFilter(request, response);
        } catch (Exception ex) {

            long time = System.currentTimeMillis() - start;

            // ✅ error log
            log.error("Error in request: {} {} | UserID: {} | Phone: {} in {} ms",
                    req.getMethod(),
                    req.getRequestURI(),
                    userId,
                    phone,
                    time,
                    ex);

            throw ex; // ⚠️ must rethrow
        } finally {

            long time = System.currentTimeMillis() - start;

            // ✅ always log (success + error)
            log.info("Completed request: {} {} -> {} in {} ms | UserID: {} | Phone: {}",
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    time,
                    userId,
                    phone);
        }
    }
}