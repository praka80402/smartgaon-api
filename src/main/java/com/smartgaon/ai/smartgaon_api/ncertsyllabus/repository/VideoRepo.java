package com.smartgaon.ai.smartgaon_api.ncertsyllabus.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.VideoContent;


public interface VideoRepo extends JpaRepository<VideoContent, Long> {

	Page<VideoContent> findByChapterId(Long chapterId, Pageable pageable);
}