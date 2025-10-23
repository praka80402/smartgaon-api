//package com.smartgaon.ai.smartgaon_api.JwtUtil;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//
//import java.util.List;
//
//@Configuration
//public class SecurityConfig {
//
//	@Bean
//	public SecurityFilterChain filter(HttpSecurity http) throws Exception {
//		http
//				.csrf(csrf -> csrf.disable())
//				.cors(cors -> cors.configurationSource(request -> {
//					CorsConfiguration config = new CorsConfiguration();
//					config.setAllowedOrigins(List.of("http://localhost:3000")); // your React app
//					config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//					config.setAllowedHeaders(List.of("*"));
//					config.setAllowCredentials(true);
//					return config;
//				}))
//				.headers(headers -> headers.frameOptions(frame -> frame.disable()))
//				.authorizeHttpRequests(auth -> auth
//						.requestMatchers( "/api/pdf/**","/api/auth/**").permitAll()
//						 .requestMatchers("/api/posts/**",
//								 "/api/comments/**", 
//								 "/api/profile/**",
//								 "/api/weather/**",
//								 "/uploads/**").permitAll()
//						.anyRequest().authenticated()
//				);
//
//		return http.build();
//	}
//
//	@Bean
//	public CorsFilter corsFilter() {
//		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//		CorsConfiguration config = new CorsConfiguration();
//		config.setAllowedOrigins(List.of("http://localhost:3000"));
//		config.setAllowedHeaders(List.of("*"));
//		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//		config.setAllowCredentials(true);
//		source.registerCorsConfiguration("/**", config);
//		return new CorsFilter(source);
//	}
//}

package com.smartgaon.ai.smartgaon_api.JwtUtil;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000")); // React app
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**", "/api/pdf/**", "/uploads/**").permitAll()
                    .requestMatchers("/api/posts/**",
                                     "/api/comments/**",
                                     "/api/profile/**",
                                     "/api/weather/**").permitAll()
                    .anyRequest().permitAll()
                );

            return http.build();
        }
    

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}


