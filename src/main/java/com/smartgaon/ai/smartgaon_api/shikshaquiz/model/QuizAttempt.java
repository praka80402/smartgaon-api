package com.smartgaon.ai.smartgaon_api.shikshaquiz.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_attempt", indexes = {
        @Index(name = "idx_attempt_user", columnList = "userId, endTime"),
        @Index(name = "idx_attempt_limit", columnList = "userId, competitionType, quizDay")
})
public class QuizAttempt {

    public enum Status { IN_PROGRESS, SUBMITTED, AUTO_SUBMITTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String segmentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SegmentType segmentType;

    // set only for COMPETITION attempts — drives the 2/day limit
    @Column(length = 50)
    private String competitionType;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    // 5 AM anchored quiz-day this attempt belongs to
    @Column(nullable = true)
    private LocalDate quizDay;

    private boolean extraTimeGranted = false;

    @Column(name = "set_number", length = 20)
    private String setNumber;

    private Integer score;

    private Integer totalQuestions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.IN_PROGRESS;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "quiz_attempt_answer", joinColumns = @JoinColumn(name = "attempt_id"))
    private List<AttemptAnswer> answers = new ArrayList<>();

    @Embeddable
    public static class AttemptAnswer {
        private Long questionId;

        @Enumerated(EnumType.STRING)
        @Column(length = 1)
        private Question.CorrectOption selectedOption; // null = unanswered on auto-submit

        private boolean isCorrect;

        public AttemptAnswer() {}

        public AttemptAnswer(Long questionId, Question.CorrectOption selectedOption, boolean isCorrect) {
            this.questionId = questionId;
            this.selectedOption = selectedOption;
            this.isCorrect = isCorrect;
        }

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public Question.CorrectOption getSelectedOption() { return selectedOption; }
        public void setSelectedOption(Question.CorrectOption selectedOption) { this.selectedOption = selectedOption; }

        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
    }

    // GETTERS & SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getSegmentKey() { return segmentKey; }
    public void setSegmentKey(String segmentKey) { this.segmentKey = segmentKey; }

    public SegmentType getSegmentType() { return segmentType; }
    public void setSegmentType(SegmentType segmentType) { this.segmentType = segmentType; }

    public String getCompetitionType() { return competitionType; }
    public void setCompetitionType(String competitionType) { this.competitionType = competitionType; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public LocalDate getQuizDay() { return quizDay; }
    public void setQuizDay(LocalDate quizDay) { this.quizDay = quizDay; }

    public boolean isExtraTimeGranted() { return extraTimeGranted; }
    public void setExtraTimeGranted(boolean extraTimeGranted) { this.extraTimeGranted = extraTimeGranted; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<AttemptAnswer> getAnswers() { return answers; }
    public void setAnswers(List<AttemptAnswer> answers) { this.answers = answers; }

    public String getSetNumber() { return setNumber; }
    public void setSetNumber(String setNumber) { this.setNumber = setNumber; }
}
