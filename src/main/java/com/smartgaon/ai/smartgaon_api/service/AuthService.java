//package com.smartgaon.ai.smartgaon_api.service;
//
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class AuthService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    public Optional<User> validateUser(String email, String password) {
//        Optional<User> userOpt = userRepository.findByEmail(email);
//        if (userOpt.isPresent()) {
//            User user = userOpt.get();
//            if (user.getPassword().equals(password)) {
//                return Optional.of(user);
//            }
//        }
//        return Optional.empty();
//    }
//}

package com.smartgaon.ai.smartgaon_api.service;

import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;
    
    @Autowired
    private EmailService emailService;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User signup(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }

    public Optional<User> validate(String email, String pass) {
        return repo.findByEmail(email)
                   .filter(u -> encoder.matches(pass, u.getPassword()));
    }
    
    public String forgotPassword(String email) {
        Optional<User> optionalUser = repo.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return "User not found";
        }

        User user = optionalUser.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        repo.save(user);

        String resetUrl = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendResetEmail(user.getEmail(), resetUrl);

        return "Password reset link sent to your email.";
    }

    // Reset Password
    public String resetPassword(String token, String newPassword) {
        Optional<User> optionalUser = repo.findByResetToken(token);
        if (optionalUser.isEmpty()) {
            return "Invalid or expired token.";
        }

        User user = optionalUser.get();
        user.setPassword(encoder.encode(newPassword));
        user.setResetToken(null);
        repo.save(user);

        return "Password reset successful.";
    }
    
    
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

   
    public User saveUser(User user) {
        return repo.save(user);
    }
}




