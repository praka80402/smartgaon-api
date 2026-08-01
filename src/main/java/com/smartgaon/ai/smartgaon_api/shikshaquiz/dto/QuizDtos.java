package com.smartgaon.ai.smartgaon_api.shikshaquiz.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question;

/** DTOs for the user-facing quiz flow (Sections 8, 9, 10). */
public class QuizDtos {

    /** Response to "start quiz" — the active batch questions + timer config. */
    public static class StartQuizResponse {
        public Long attemptId;
        public String segmentKey;
        public List<QuestionDtos.QuestionPublicDto> questions;
        public Integer timeLimitMinutes;        // null = no timer
        public Integer extraTimeAllowedMinutes; // one-time grant, Competition only
        public long secondsToNextRotation;
    }

    /** Returned instead of StartQuizResponse when the daily attempt limit is hit. */
    public static class AttemptLimitResponse {
        public boolean limitReached = true;
        public String message; // "You've completed your 2 attempts... available in 3h 40m."
        public String nextRotationIn;
        public long secondsToNextRotation;
    }

    public static class SubmitQuizRequest {
        public Long attemptId;
        public Integer timeTakenSeconds;
        public List<AnswerDto> answers;
    }

    public static class AnswerDto {
        public Long questionId;
        public Question.CorrectOption selectedOption; // null = left unanswered
    }

    /** Summary modal payload: score + full question-by-question review (Section 9). */
    public static class QuizResultResponse {
        public Long attemptId;
        public int score;
        public int totalQuestions;
        public int correctCount;
        public int incorrectCount;
        public double percentage;
        public boolean autoSubmitted; // time expired server-side
        public List<ReviewItemDto> review;
    }

    public static class ReviewItemDto {
        public Long questionId;
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public Question.CorrectOption selectedOption;
        public Question.CorrectOption correctOption; // revealed only after submission
        public boolean isCorrect;
    }

    /** Last-5 performance dashboard (Section 10). */
    public static class PerformanceResponse {
        public List<AttemptSummaryDto> attempts;
        public OverallStatsDto overallStats;
    }

    public static class AttemptSummaryDto {
        public String segmentKey;
        public int score;
        public int totalQuestions;
        public double percentage;
        public LocalDateTime date;
    }

    public static class OverallStatsDto {
        public double avgPercentage;
        public double bestScore;
        public long totalQuizzesTaken;
        public String trend; // IMPROVING | DECLINING | STABLE
    }

    public static class CountdownResponse {
        public long secondsToNextRotation;
        public String display; // "3h 40m"
        public LocalDateTime nextSlotTime;
    }

    /** Stock alert card for the admin dashboard (Section 5.2). */
    public static class StockStatusDto {
        public String segmentKey;
        public long scheduledBatchCount;
        public long daysOfStock;
        public String status; // HEALTHY | LOW | CRITICAL
        public String displayColor; // GREEN | ORANGE | RED
    }

    /** Batch builder request (Section 4.3). */
    public static class BatchBuildRequest {
        public String segmentKey;      // e.g. "CLASS_10_MATH" or "SSC"
        public LocalDate startDate;    // first quiz-day to schedule
        public int days;               // e.g. 30 -> 180 batches
        public Integer questionsPerBatch; // null = take from CompetitionConfig / segment default
        public boolean autoFill = true;   // false = create empty batches for manual assignment
    }
}
