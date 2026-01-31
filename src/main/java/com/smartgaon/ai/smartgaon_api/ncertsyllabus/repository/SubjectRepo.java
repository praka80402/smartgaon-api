package com.smartgaon.ai.smartgaon_api.ncertsyllabus.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.*;
public interface SubjectRepo extends JpaRepository<Subject, Long> {

	List<Subject> findBySchoolClass_Id(Long classId);
}

