//package com.smartgaon.ai.smartgaon_api.admin.config;
//
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import com.smartgaon.ai.smartgaon_api.admin.service.AdminService;
//import com.smartgaon.ai.smartgaon_api.admin.model.Admin;
//
//@Component
//@RequiredArgsConstructor
//public class DefaultAdminConfig {
//
//    private final AdminService adminService;
//
//    @PostConstruct
//    public void createDefaultAdmin() {
//
//        String defaultEmail = "admin@smartgaon.com";
//
//        // If admin exists → do nothing
//        if (adminService.findByEmail(defaultEmail).isPresent()) {
//            return;
//        }
//
//        Admin admin = Admin.builder()
//                .name("Super Admin")
//                .email(defaultEmail)
//                .password("admin123")    // PLAIN TEXT — AdminService will hash it
//                .role("ADMIN")
//                .build();
//
//        adminService.createAdmin(admin);
//
//        System.out.println("Default ADMIN created:");
//        System.out.println("Email: admin@smartgaon.com");
//        System.out.println("Password: admin123");
//    }
//}
