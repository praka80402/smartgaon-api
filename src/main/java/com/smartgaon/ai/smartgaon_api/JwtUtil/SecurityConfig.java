package com.smartgaon.ai.smartgaon_api.JwtUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Configuration
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
//                .authorizeHttpRequests(auth -> auth
//                        // 1️⃣ Public Admin login/register
//                        .requestMatchers("/api/admin/login", "/api/admin/register").permitAll()
//
//                        // 2️⃣ Public user access
//                        .requestMatchers(HttpMethod.GET, "/api/community/events/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/api/community/news/**").permitAll()
//                        .requestMatchers("/api/auth/**", "/uploads/**", "/api/pdf/**").permitAll()
//
//                        // 3️⃣ Admin-only endpoints (must be ABOVE /api/admin/**)
//                        .requestMatchers("/api/admin/users/**").hasAuthority("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/community/events/**").hasAuthority("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/community/events/**").hasAuthority("ADMIN")
//                        .requestMatchers(HttpMethod.POST, "/api/community/news/**").hasAuthority("ADMIN")
//                        .requestMatchers(HttpMethod.DELETE, "/api/community/news/**").hasAuthority("ADMIN")
//
//                        // 4️⃣ Protect all other admin URLs
//                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
//
//                        // 5️⃣ Allow all remaining endpoints
//                        .anyRequest().permitAll()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/community/**").permitAll()
                        .requestMatchers("/api/auth/**", "/api/pdf/**").permitAll()
                        .anyRequest().permitAll()
                
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
