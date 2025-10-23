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

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;


    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User signup(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        return repo.save(u);
    }

    public Optional<User> validate(String email, String pass) {
        return repo.findByEmail(email)
                   .filter(u -> encoder.matches(pass, u.getPassword()));
    }
    
    
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    // ✅ Save updated user (for bio, village, image)
    public User saveUser(User user) {
        return repo.save(user);
    }
}




