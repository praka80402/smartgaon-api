package com.smartgaon.ai.smartgaon_api.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartgaon.ai.smartgaon_api.admin.service.AdminService;
import com.smartgaon.ai.smartgaon_api.admin.dto.AdminLoginRequest;
import com.smartgaon.ai.smartgaon_api.admin.dto.AdminRegisterRequest;
import com.smartgaon.ai.smartgaon_api.admin.model.Admin;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AdminRegisterRequest req) {
        if (adminService.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        Admin admin = Admin.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(req.getPassword())
                .build();
        Admin saved = adminService.createAdmin(admin);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AdminLoginRequest req) {

        System.out.println("====== ADMIN LOGIN DEBUG ======");
        System.out.println("Email from request: " + req.getEmail());
        System.out.println("Password from request: " + req.getPassword());

        var adminOpt = adminService.findByEmail(req.getEmail());

        if (adminOpt.isEmpty()) {
            System.out.println("❌ Admin not found in DB for: " + req.getEmail());
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        Admin admin = adminOpt.get();
        System.out.println("Stored hashed password: " + admin.getPassword());

        boolean matches = passwordEncoder.matches(req.getPassword(), admin.getPassword());
        System.out.println("Password matches? " + matches);

        if (!matches) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        String token = jwtUtil.generate(admin.getEmail(), "ADMIN");
        System.out.println("✅ Login success! Token generated.");

        return ResponseEntity.ok(new AuthResponse(token));
    }

    // response record — MUST be inside the controller class
    record AuthResponse(String token) {}
}
