package com.smartgaon.ai.smartgaon_api.doctor;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialtyAndActiveTrue(String specialty);

    List<Doctor> findByActiveTrue();
}
