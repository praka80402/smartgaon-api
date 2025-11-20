

package com.smartgaon.ai.smartgaon_api.auth.controller;

//import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.jackson2.JacksonFactory;
//import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.service.AuthService;
//
//import java.io.InputStream;
//import java.io.ByteArrayOutputStream;
//import java.net.URL;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
//import org.springframework.beans.factory.annotation.Value;
//
//import org.springframework.web.bind.annotation.*;
//
//
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
//public class AuthController {
//    @Autowired 
//    private AuthService auth;
//    @Autowired 
//    private JwtUtil jwt;
//    
////    @Value("${google.client.web-id}")
////    private String webClientId;
//
//    @Value("${google.client.native-id}")
//    private String nativeClientId;
//    
//
//
//    @PostMapping("/signup")
//    
//    public ResponseEntity<String> signup(@RequestBody Map<String, String> req) {
//        User user = new User();
//        user.setFirstName(req.get("firstName"));
//        user.setLastName(req.get("lastName"));
//        user.setEmail(req.get("email"));
//        user.setPhone(req.get("phone"));
//        user.setPassword(req.get("password"));
//
//        auth.signup(user);
//        return ResponseEntity.ok("User registered successfully");
//    }
//
//
////    @PostMapping("/login")
////    public ResponseEntity<String> login(@RequestBody Map<String,String> loginData) {
////        String email = loginData.get("email");
////        String password = loginData.get("password");
////
////        return auth.validate(email, password)
////                   .map(u -> ResponseEntity.ok(jwt.generate(email)))  
////                   .orElseGet(() -> ResponseEntity.status(401).body("Invalid email or password"));
////    }
//    
//    
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
//        String email = loginData.get("email");
//        String password = loginData.get("password");
//
//        return auth.validate(email, password)
//                .map(user -> {
//                    String token = jwt.generate(email);
//
//                    Map<String, Object> response = new HashMap<>();
//                    response.put("token", token);
//                    response.put("user", user); // ✅ send user details including id, email, etc.
//
//                    return ResponseEntity.ok(response);
//                })
//                .orElseGet(() -> {
//                    Map<String, Object> error = new HashMap<>();
//                    error.put("error", "Invalid email or password");
//                    return ResponseEntity.status(401).body(error);
//                });
//    }
//    
//
////----------------------------------------------------------------------------------
////   working properly in web singup with google
//    @PostMapping("/google")
//    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
//        String idToken = body.get("token");
//
//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
//                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
//                .setAudience(Collections.singletonList(nativeClientId))
//                .build();
//
//        try {
//            GoogleIdToken googleIdToken = verifier.verify(idToken);
//            if (googleIdToken == null) {
//                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google token"));
//            }
//
//            GoogleIdToken.Payload payload = googleIdToken.getPayload();
//            String email = payload.getEmail();
//            String name = (String) payload.get("name");
//            String picture = (String) payload.get("picture");
//
//            Optional<User> existingUser = auth.findByEmail(email);
//            User user;
//            if (existingUser.isEmpty()) {
//                user = new User();
//                user.setFirstName(name);
//                user.setEmail(email);
//                user.setPassword(null);
//                user.setProfileImage(null);
//                auth.saveUser(user);
//            } else {
//                user = existingUser.get();
//            }
//
//            String jwtToken = jwt.generate(email);
//            return ResponseEntity.ok(Map.of("token", jwtToken, "email", email, "name", name, "picture", picture));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Verification failed"));
//        }
//    }
////    --------------------------------------------------------
//    
//    //  Forgot Password API
//    @PostMapping("/forgot-password")
//    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
//        try {
//            String message = auth.forgotPassword(email);
//            return ResponseEntity.ok(message);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    //  Reset Password API
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestParam String token,
//                                                @RequestParam String newPassword) {
//        try {
//            String message = auth.resetPassword(token, newPassword);
//            return ResponseEntity.ok(message);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
//
//    
//    
//    
//    
//}
//-------------------------------------------------------



import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.smartgaon.ai.smartgaon_api.JwtUtil.JwtUtil;
import com.smartgaon.ai.smartgaon_api.auth.service.AuthService;
import com.smartgaon.ai.smartgaon_api.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")

@CrossOrigin(origins = "*")

public class AuthController {

    @Autowired
    private AuthService auth;
    @Autowired
    private JwtUtil jwt;

    @Value("${google.client.native-id}")
    private String nativeClientId;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, String> req) {
        User user = new User();
        user.setFirstName(req.get("firstName"));
        user.setLastName(req.get("lastName"));
        user.setEmail(req.get("email"));
        user.setPhone(req.get("phone"));
        user.setPassword(req.get("password"));
        auth.signup(user);
        return ResponseEntity.ok("User registered successfully");
    }
    
    @PostMapping("/signup-phone")
    public ResponseEntity<?> signupWithPhone(@RequestParam String phone) {

        // ❌ Invalid phone number
        if (!phone.matches("^[6-9]\\d{9}$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Please enter a valid 10-digit mobile number.")
            );
        }

       
        Optional<User> existingUser = auth.findByPhone(phone);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(409).body(
                    Map.of(
                            "error", "This number is already registered",
                            "navigate", "login"
                    )
            );
        }

        // ✅ Generate OTP & create new user
        Map<String, Object> resp = auth.generateSignupOtp(phone);
        return ResponseEntity.ok(resp);
    }
//----------------------------------------------

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        return auth.validate(email, password)
                .map(user -> {
                    String token = jwt.generate(email);
                    Map<String, Object> response = new HashMap<>();
                    response.put("token", token);
                    response.put("user", user);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Invalid email or password")));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> body) {
        String idToken = body.get("token");

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(nativeClientId))
                .build();

        try {
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid Google token"));

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            Optional<User> existingUser = auth.findByEmail(email);
            User user = existingUser.orElseGet(() -> {
                User newUser = new User();
                newUser.setFirstName(name);
                newUser.setEmail(email);
                auth.saveUser(newUser);
                return newUser;
            });

            String jwtToken = jwt.generate(email);
            return ResponseEntity.ok(Map.of("token", jwtToken, "email", email, "name", name, "picture", picture));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Verification failed"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            return ResponseEntity.ok(auth.forgotPassword(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        try {
            return ResponseEntity.ok(auth.resetPassword(token, newPassword));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }



    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestParam String mobile) {
        return auth.sendOtp(mobile); // 🟢 This handles login logic
    }

    
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestParam String mobile, @RequestParam String otp) {
        Map<String, Object> response = auth.verifyOtp(mobile, otp);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/by-pincode/{pincode}")
    public ResponseEntity<?> getUsersByPincode(@PathVariable("pincode") String pincode) {
        return ResponseEntity.ok(auth.getUsersByPinCode(pincode));
    }


}












