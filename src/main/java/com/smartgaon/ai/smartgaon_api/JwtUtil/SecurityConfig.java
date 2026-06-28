package com.smartgaon.ai.smartgaon_api.JwtUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();

                    // IMPORTANT: allow all Vercel domains
                    config.setAllowedOriginPatterns(List.of(
                            "*"
//                        "https://*.vercel.app"        // All Vercel URLs
                    ));

                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                //    config.setAllowCredentials(true);
                    config.setExposedHeaders(List.of("*"));

                    return config;
                }))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth

                        // ---- Always allow CORS pre-flight (OPTIONS) ----
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ===========================================================
                        // PUBLIC (no login)
                        // ===========================================================

                        // Auth + OTP + system
                        .requestMatchers("/api/auth/**", "/api/otp/**").permitAll()
                        .requestMatchers("/uploads/**", "/api/pdf/**", "/api/weather/**").permitAll()

                        // SmartGaon (villages) — fully public
                        .requestMatchers("/api/villages/**").permitAll()

                        // Media & Press — fully public
                        .requestMatchers("/api/media-gallery/**").permitAll()

                        // Stay Enquiry — public (guest enquiry)
                        .requestMatchers("/api/enquiries/**").permitAll()

                        // Donation — sirf public listing dikhe; personal history/receipt login ke baad
                        .requestMatchers(HttpMethod.GET, "/api/donations/projects", "/api/donations/programs").permitAll()
                       // /my/**, /receipt/**, /yearly/** → automatically protected (anyRequest().authenticated() se)

                        // Gaon Talent — VIEW only public; like/comment/post (POST/PUT/DELETE) needs login
                        .requestMatchers(HttpMethod.GET, "/api/gaon-talent/**").permitAll()

                        // ===========================================================
                        // ADMIN endpoints — left OPEN for now (managed by a separate system)
                        // ===========================================================
                        .requestMatchers("/api/admin/**", "/admin/**", "/offers/**").permitAll()

                        // ===========================================================
                        // EVERYTHING ELSE — login required
                        // (profile, jobs, business, forum, events, banners, user/**,
                        //  community, donations POST, gaon-talent POST, etc.)
                        // ===========================================================
                        .anyRequest().authenticated()
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("*")); // allow all apps, solves expo issue
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
     //   config.setAllowCredentials(false);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

}