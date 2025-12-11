//package com.smartgaon.ai.smartgaon_api.admin.controller;
//
//import com.smartgaon.ai.smartgaon_api.auth.service.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/admin/users")
//@RequiredArgsConstructor
//public class AdminUserController {
//
//    private final AuthService authService;
//
//    @GetMapping
//    public ResponseEntity<?> getAllUsers() {
//        return ResponseEntity.ok(authService.getAllUsers());
//    }
//
//    @GetMapping("/{phone}")
//    public ResponseEntity<?> getUserByPhone(@PathVariable String phone) {
//        return ResponseEntity.ok(authService.findByPhone(phone));
//    }
//
//    @DeleteMapping("/{phone}")
//    public ResponseEntity<?> deleteUser(@PathVariable String phone) {
//        boolean deleted = authService.deleteUserByPhone(phone);
//
//        if (!deleted) {
//            return ResponseEntity.status(404).body("User not found");
//        }
//
//        return ResponseEntity.ok("User deleted successfully");
//    }
//}
