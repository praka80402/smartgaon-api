package com.smartgaon.ai.smartgaon_api.successstory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.successstory.model.SuccessStory;


public interface SuccessStoryRepository
extends JpaRepository<SuccessStory, Long> {

List<SuccessStory> findByPublishedTrueOrderByCreatedAtDesc();
}

