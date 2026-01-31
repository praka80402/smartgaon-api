package com.smartgaon.ai.smartgaon_api.ncertsyllabus.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.*;
public interface ChapterRepo extends JpaRepository<Chapter, Long> {

    List<Chapter> findBySubjectId(Long subjectId);
}
