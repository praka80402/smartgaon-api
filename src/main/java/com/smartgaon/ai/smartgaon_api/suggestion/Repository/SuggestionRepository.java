package com.smartgaon.ai.smartgaon_api.suggestion.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.suggestion.Entity.Suggestion;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {
    List<Suggestion> findByPincodeOrderByCreatedAtDesc(String pincode);
    List<Suggestion> findByPhoneOrderByCreatedAtDesc(String phone);
}

