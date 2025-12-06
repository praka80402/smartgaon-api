package com.smartgaon.ai.smartgaon_api.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.smartgaon.ai.smartgaon_api.admin.model.Admin;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
}

