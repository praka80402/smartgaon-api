package com.smartgaon.ai.smartgaon_api.ncertsyllabus.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.PdfContent;

public interface PdfRepo extends JpaRepository<PdfContent, Long> {

	Page<PdfContent> findByChapterId(Long chapterId, Pageable pageable);
}
