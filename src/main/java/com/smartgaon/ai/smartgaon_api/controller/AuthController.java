//package com.smartgaon.ai.smartgaon_api.controller;
//
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.service.AuthService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    @Autowired
//    private AuthService authService;
//
//    @PostMapping("/login")
//    public ResponseEntity<String> login(@RequestBody Map<String, String> loginData) {
//        String email = loginData.get("email");
//        String password = loginData.get("password");
//        Optional<User> userOpt = authService.validateUser(email, password);
//
//        if (userOpt.isPresent()) {
//            return ResponseEntity.ok("Login successful");
//        } else {
//            return ResponseEntity.status(401).body("Invalid email or password");
//        }
//    }
//}

package com.smartgaon.ai.smartgaon_api.controller;

import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {
    @Autowired private AuthService auth;
    @Autowired private JwtUtil jwt;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User u) {
        auth.signup(u);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String,String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        return auth.validate(email, password)
                   .map(u -> ResponseEntity.ok(jwt.generate(email)))  // returns plain token
                   .orElseGet(() -> ResponseEntity.status(401).body("Invalid email or password"));
    }
}