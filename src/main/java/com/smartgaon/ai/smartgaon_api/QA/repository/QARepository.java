package com.smartgaon.ai.smartgaon_api.QA.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.QA.model.QA;



@Repository
public interface QARepository extends JpaRepository<QA, Long> {

    @Query(value = "SELECT * FROM qa_data WHERE MATCH(question) AGAINST (?1 IN NATURAL LANGUAGE MODE) LIMIT 1", nativeQuery = true)
    List<QA> searchByQuestion(String question);

  
    boolean existsByQuestion(String question);
}