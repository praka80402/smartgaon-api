package com.smartgaon.ai.smartgaon_api.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.smartgaon.ai.smartgaon_api.admin.repository.AdminRepository;
import com.smartgaon.ai.smartgaon_api.admin.model.Admin;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository repo;
    private final PasswordEncoder passwordEncoder;

    public Admin createAdmin(Admin admin) {
        // hash password before saving (single hash)
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole("ADMIN");
        return repo.save(admin);
    }

    public Optional<Admin> findByEmail(String email) {
        return repo.findByEmail(email);
    }
}
