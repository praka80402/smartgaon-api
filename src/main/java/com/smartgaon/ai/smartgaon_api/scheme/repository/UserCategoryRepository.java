package com.smartgaon.ai.smartgaon_api.scheme.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.scheme.entity.Category;



public interface UserCategoryRepository extends JpaRepository<Category, Long> {
}