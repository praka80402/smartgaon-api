package com.smartgaon.ai.smartgaon_api.business;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessPostRepository
        extends JpaRepository<BusinessPost, Long> {

    List<BusinessPost> findByUserIdOrderByCreatedAtDesc(Long userId);
}
