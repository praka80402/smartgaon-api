package com.smartgaon.ai.smartgaon_api.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
    // Auth - Login / Signup - AuthModal.jsx
    "/api/admin/login",
    "/api/admin/register",
    "/api/auth/send-otp",
    "/api/auth/signup-phone",
    "/api/auth/send-signup-otp",
    "/api/auth/verify-otp",
    "/api/auth/generate-jwt-token",
    "/api/auth/refresh",
    "/api/otp/send",
    "/api/otp/verify",
    "/api/auth/logout",
    // Profile - CompleteProfile.jsx + AuthModal.jsx
    "/api/profile",
    "/api/profile/**",
    "/api/profile/update",
    "/api/profile/upload-image/**",

    // Location - CompleteProfile.jsx
    "/api/location/**",
    "/api/states",              // FIX: was "api/states" (missing leading slash, never matched)

    // Baki tumhare public wale
    "/api/public/**",
    "/api/quiz/groq-key/raw",
    "/api/event-banners/section/LANDING_BANNER",
    "/api/school-competitions/**",
    "/api/gaon-talent/**",
    "/api/media-gallery/**",
    "/api/states/by-name",
    "/api/donations/projects",
    "/api/donations/programs",
    "/admin/dashboard",
                        "/api/villages/sg/smart",
                        "/api/villages/sg",
                        "/api/villages/sg/**",
                        "/api/villages/sg/find",
                        "/api/villages",
                        "/api/villages/**",

                        "/api/enquiries",
                        "/api/enquiries/**"

                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
