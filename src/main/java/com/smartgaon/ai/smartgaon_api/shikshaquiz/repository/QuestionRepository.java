package com.smartgaon.ai.smartgaon_api.shikshaquiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    boolean existsByQuestionHash(String questionHash);

    List<Question> findBySegmentTypeAndSubjectAndIsActiveTrue(SegmentType segmentType, String subject);

    List<Question> findBySegmentTypeAndClassLevelAndSubjectAndStatusAndIsActiveTrue(
            SegmentType segmentType, String classLevel, String subject, Question.Status status);

    List<Question> findBySegmentTypeAndCompetitionTypeAndStatusAndIsActiveTrue(
            SegmentType segmentType, String competitionType, Question.Status status);

    List<Question> findBySegmentType(SegmentType segmentType);
}
