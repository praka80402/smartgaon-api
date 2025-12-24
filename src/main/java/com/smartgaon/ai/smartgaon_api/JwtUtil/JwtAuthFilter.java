package com.smartgaon.ai.smartgaon_api.JwtUtil;

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

                SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);
                
                // 🔍 DEBUG LOGS
                System.out.println("===== JWT DEBUG =====");
                System.out.println("EMAIL = " + email);
                System.out.println("ROLE = " + role);
                System.out.println("AUTHORITY ADDED = " + authority.getAuthority());
                
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(email, null, List.of(authority));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception e) {
                // ignore invalid tokens
            }
        }

        chain.doFilter(request, response);
    }
    
  

}
