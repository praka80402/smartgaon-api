package com.smartgaon.ai.smartgaon_api.shikshaquiz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_question", indexes = {
        @Index(name = "idx_question_segment", columnList = "segmentType, classLevel, competitionType, subject"),
        @Index(name = "idx_question_hash", columnList = "questionHash")
})
public class Question {

    public enum CorrectOption { A, B, C, D }
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Status { DRAFT, PUBLISHED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SegmentType segmentType;

    // "5".."12" — Academic only
    @Column(length = 10)
    private String classLevel;

    // "SSC", "GENERAL", "STATE_EXAM" — Competition only
    @Column(length = 50)
    private String competitionType;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionText;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String optionA;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String optionB;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String optionC;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String optionD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 1)
    private CorrectOption correctOption;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Difficulty difficulty; // optional

    // book/article reference (admin use)
    @Column(length = 255)
    private String sourceRef;

    // SHA-256(normalizedText + subject + classLevel/competitionType)
    @Column(length = 64, nullable = false)
    private String questionHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.PUBLISHED;

    private LocalDateTime createdAt = LocalDateTime.now();

    private boolean isActive = true;

    // GETTERS & SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SegmentType getSegmentType() { return segmentType; }
    public void setSegmentType(SegmentType segmentType) { this.segmentType = segmentType; }

    public String getClassLevel() { return classLevel; }
    public void setClassLevel(String classLevel) { this.classLevel = classLevel; }

    public String getCompetitionType() { return competitionType; }
    public void setCompetitionType(String competitionType) { this.competitionType = competitionType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public CorrectOption getCorrectOption() { return correctOption; }
    public void setCorrectOption(CorrectOption correctOption) { this.correctOption = correctOption; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public String getSourceRef() { return sourceRef; }
    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }

    public String getQuestionHash() { return questionHash; }
    public void setQuestionHash(String questionHash) { this.questionHash = questionHash; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
