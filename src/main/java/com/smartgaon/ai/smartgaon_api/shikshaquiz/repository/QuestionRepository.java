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

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT q.setNumber FROM Question q WHERE q.segmentType = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType.ACADEMIC AND q.classLevel = :classLevel AND (:subject IS NULL OR q.subject = :subject) AND q.status = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question.Status.PUBLISHED AND q.isActive = true AND q.setNumber IS NOT NULL")
    List<String> findDistinctSetNumbersForAcademic(@org.springframework.data.repository.query.Param("classLevel") String classLevel, @org.springframework.data.repository.query.Param("subject") String subject);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT q.setNumber FROM Question q WHERE q.segmentType = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType.COMPETITION AND q.competitionType = :competitionType AND q.status = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question.Status.PUBLISHED AND q.isActive = true AND q.setNumber IS NOT NULL")
    List<String> findDistinctSetNumbersForCompetition(String competitionType);

    List<Question> findBySegmentTypeAndClassLevelAndSubjectAndSetNumberAndStatusAndIsActiveTrue(
            SegmentType segmentType, String classLevel, String subject, String setNumber, Question.Status status);

    @org.springframework.data.jpa.repository.Query("SELECT q FROM Question q WHERE q.segmentType = :segmentType AND q.classLevel = :classLevel AND (:subject IS NULL OR q.subject = :subject) AND q.setNumber = :setNumber AND q.status = :status AND q.isActive = true")
    List<Question> findAcademicQuestions(
            @org.springframework.data.repository.query.Param("segmentType") SegmentType segmentType,
            @org.springframework.data.repository.query.Param("classLevel") String classLevel,
            @org.springframework.data.repository.query.Param("subject") String subject,
            @org.springframework.data.repository.query.Param("setNumber") String setNumber,
            @org.springframework.data.repository.query.Param("status") Question.Status status);

    List<Question> findBySegmentTypeAndCompetitionTypeAndSetNumberAndStatusAndIsActiveTrue(
            SegmentType segmentType, String competitionType, String setNumber, Question.Status status);

    @org.springframework.data.jpa.repository.Query("SELECT MAX(q.setNumber) FROM Question q WHERE q.segmentType = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType.ACADEMIC AND q.classLevel = :classLevel AND (:subject IS NULL OR q.subject = :subject)")
    String findMaxSetNumberForAcademic(@org.springframework.data.repository.query.Param("classLevel") String classLevel, @org.springframework.data.repository.query.Param("subject") String subject);

    @org.springframework.data.jpa.repository.Query("SELECT MAX(q.setNumber) FROM Question q WHERE q.segmentType = com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType.COMPETITION AND q.competitionType = :competitionType")
    String findMaxSetNumberForCompetition(String competitionType);
}
