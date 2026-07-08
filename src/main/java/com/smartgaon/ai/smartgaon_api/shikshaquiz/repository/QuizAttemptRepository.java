package com.smartgaon.ai.smartgaon_api.shikshaquiz.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuizAttempt;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    // fallback attempt-limit check when Redis is unavailable
    long countByUserIdAndCompetitionTypeAndQuizDay(Long userId, String competitionType, LocalDate quizDay);

    List<QuizAttempt> findTop5ByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(Long userId);

    long countByUserIdAndEndTimeIsNotNull(Long userId);
}
