package com.smartgaon.ai.smartgaon_api.shikshaquiz.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuestionDtos.QuestionPublicDto;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuizDtos.*;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.CompetitionConfig;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuizAttempt;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.CompetitionConfigRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuestionRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuizAttemptRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.util.QuizTimeUtil;

/**
 * User-facing quiz flow: start -> (optional extra time) -> batched submit + scoring.
 * Sets of 10 questions are allocated randomly without repetition using Redis.
 */
@Service
public class QuizService {

    public static final String COMPLETED_SETS_KEY_PREFIX = "quiz:user_completed_sets:";
    /** grace period for network latency when validating elapsed time */
    public static final int SUBMIT_GRACE_SECONDS = 30;

    @Autowired private QuizAttemptRepository attemptRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private CompetitionConfigRepository configRepo;
    @Autowired private QuizRedisService redis;

    // ---------- Start quiz ----------

    @Transactional
    public Object startQuiz(Long userId, String segmentKey, String language) {
        String lang = (language != null && !language.trim().isEmpty()) ? language.trim().toUpperCase() : "EN";
        LocalDateTime now = LocalDateTime.now();
        SegmentType segmentType = segmentKey.startsWith("CLASS_")
                ? SegmentType.ACADEMIC : SegmentType.COMPETITION;
        String competitionType = segmentType == SegmentType.COMPETITION ? segmentKey.toUpperCase().replace(" ", "_") : null;

        CompetitionConfig config = configRepo.findByCompetitionType(
                segmentType == SegmentType.COMPETITION ? competitionType : academicConfigKey(segmentKey))
                .orElse(null);

        // Fetch all available set numbers for this segment and language
        List<String> allSetNumbers;
        String classLevel = null;
        String subject = null;
        if (segmentType == SegmentType.ACADEMIC) {
            if (segmentKey.equals("CLASS_UNDER_5")) {
                classLevel = "UNDER_5";
                subject = null;
            } else if (segmentKey.equals("CLASS_6_TO_8")) {
                classLevel = "6_TO_8";
                subject = null;
            } else if (segmentKey.equals("CLASS_9_TO_12")) {
                classLevel = "9_TO_12";
                subject = null;
            } else if (segmentKey.startsWith("CLASS_UNDER_5_")) {
                classLevel = "UNDER_5";
                subject = segmentKey.substring("CLASS_UNDER_5_".length()).replace("_", " ");
            } else if (segmentKey.startsWith("CLASS_6_TO_8_")) {
                classLevel = "6_TO_8";
                subject = segmentKey.substring("CLASS_6_TO_8_".length()).replace("_", " ");
            } else if (segmentKey.startsWith("CLASS_9_TO_12_")) {
                classLevel = "9_TO_12";
                subject = segmentKey.substring("CLASS_9_TO_12_".length()).replace("_", " ");
            } else {
                String rest = segmentKey.substring("CLASS_".length());
                int underscore = rest.indexOf('_');
                classLevel = underscore > 0 ? rest.substring(0, underscore) : rest;
                subject = underscore > 0 ? rest.substring(underscore + 1).replace("_", " ") : null;
            }
            allSetNumbers = questionRepo.findDistinctSetNumbersForAcademicAndLanguage(classLevel, subject, lang);
        } else {
            allSetNumbers = questionRepo.findDistinctSetNumbersForCompetitionAndLanguage(competitionType, lang);
        }

        if (allSetNumbers == null || allSetNumbers.isEmpty()) {
            AttemptLimitResponse limitResp = new AttemptLimitResponse();
            limitResp.limitReached = true;
            limitResp.message = "No quiz questions uploaded yet for this category. Please check back soon!";
            return limitResp;
        }

        // Check unattempted sets via Redis with DB fallback
        String redisKey = COMPLETED_SETS_KEY_PREFIX + userId + ":" + segmentKey + ":" + lang;
        List<String> unattemptedSets = new ArrayList<>();
        for (String setNum : allSetNumbers) {
            Boolean isCompleted = redis.setContains(redisKey, setNum);
            if (isCompleted == null) {
                // Redis offline -> DB fallback
                boolean exists = attemptRepo.existsByUserIdAndSegmentKeyAndSetNumber(userId, segmentKey, setNum);
                if (!exists) {
                    unattemptedSets.add(setNum);
                }
            } else if (!isCompleted) {
                unattemptedSets.add(setNum);
            }
        }

        if (unattemptedSets.isEmpty()) {
            AttemptLimitResponse limitResp = new AttemptLimitResponse();
            limitResp.message = "You have completed all quiz sets in this category! Please try another category.";
            return limitResp;
        }

        // Randomly select one unattempted set
        int randomIndex = new java.util.Random().nextInt(unattemptedSets.size());
        String selectedSetNumber = unattemptedSets.get(randomIndex);

        // Fetch questions for selected set and language
        List<Question> questions;
        if (segmentType == SegmentType.ACADEMIC) {
            questions = questionRepo.findAcademicQuestionsAndLanguage(
                    SegmentType.ACADEMIC, classLevel, subject, selectedSetNumber, Question.Status.PUBLISHED, lang);
        } else {
            questions = questionRepo.findBySegmentTypeAndCompetitionTypeAndSetNumberAndStatusAndLanguageAndIsActiveTrue(
                    SegmentType.COMPETITION, competitionType, selectedSetNumber, Question.Status.PUBLISHED, lang);
        }

        if (questions == null || questions.isEmpty()) {
            throw new IllegalStateException("Selected quiz set " + selectedSetNumber + " has no questions");
        }

        if (questions.size() > 10) {
            java.util.Collections.shuffle(questions);
            questions = questions.subList(0, 10);
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setSegmentKey(segmentKey);
        attempt.setSegmentType(segmentType);
        attempt.setCompetitionType(competitionType);
        attempt.setStartTime(now);
        attempt.setSetNumber(selectedSetNumber);
        attempt.setTotalQuestions(questions.size());
        attempt = attemptRepo.save(attempt);

        StartQuizResponse resp = new StartQuizResponse();
        resp.attemptId = attempt.getId();
        resp.segmentKey = segmentKey;
        resp.secondsToNextRotation = 0; // Rotation removed
        resp.questions = questions.stream().map(QuestionPublicDto::from).toList();
        if (config != null) {
            resp.timeLimitMinutes = config.getTimeLimitMinutes();
            resp.extraTimeAllowedMinutes = config.getExtraTimeAllowedMinutes();
        }
        return resp;
    }

    // ---------- One-time extra time (Section 8.1) ----------

    @Transactional
    public String grantExtraTime(Long attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + attemptId));
        if (attempt.getStatus() != QuizAttempt.Status.IN_PROGRESS) {
            throw new IllegalStateException("Quiz already submitted");
        }
        if (attempt.isExtraTimeGranted()) {
            throw new IllegalStateException("Extra time can be granted only once per attempt");
        }
        attempt.setExtraTimeGranted(true);
        attemptRepo.save(attempt);
        return "Extra time granted";
    }

    // ---------- Batched submit + server-side scoring (Sections 8.1 & 9) ----------

    @Transactional
    public QuizResultResponse submitQuiz(SubmitQuizRequest req) {
        LocalDateTime now = LocalDateTime.now();
        QuizAttempt attempt = attemptRepo.findById(req.attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found: " + req.attemptId));
        if (attempt.getStatus() != QuizAttempt.Status.IN_PROGRESS) {
            throw new IllegalStateException("Quiz already submitted");
        }

        // ----- Server-side elapsed-time validation: startTime + timeLimit + extraTime >= submitTime -----
        boolean autoSubmitted = false;
        CompetitionConfig config = configRepo.findByCompetitionType(
                attempt.getSegmentType() == SegmentType.COMPETITION
                        ? attempt.getCompetitionType()
                        : academicConfigKey(attempt.getSegmentKey()))
                .orElse(null);

        if (config != null && config.getTimeLimitMinutes() != null) {
            LocalDateTime deadline = attempt.getStartTime()
                    .plusMinutes(config.getTimeLimitMinutes());
            if (attempt.isExtraTimeGranted() && config.getExtraTimeAllowedMinutes() != null) {
                deadline = deadline.plusMinutes(config.getExtraTimeAllowedMinutes());
            }
            deadline = deadline.plusSeconds(SUBMIT_GRACE_SECONDS);
            if (now.isAfter(deadline)) {
                autoSubmitted = true; // accepted, but flagged — client timer was tampered/expired
            }
        }

        // ----- Score against the DB — the client never knew correctOption -----
        List<Question> questions = questionRepo.findAllById(
                req.answers.stream().map(a -> a.questionId).toList());
        Map<Long, Question> byId = new HashMap<>();
        for (Question q : questions) byId.put(q.getId(), q);

        int correct = 0;
        List<QuizAttempt.AttemptAnswer> saved = new ArrayList<>();
        List<ReviewItemDto> review = new ArrayList<>();

        for (AnswerDto a : req.answers) {
            Question q = byId.get(a.questionId);
            if (q == null) continue;
            boolean isCorrect = a.selectedOption != null && a.selectedOption == q.getCorrectOption();
            if (isCorrect) correct++;

            saved.add(new QuizAttempt.AttemptAnswer(q.getId(), a.selectedOption, isCorrect));

            ReviewItemDto item = new ReviewItemDto();
            item.questionId = q.getId();
            item.questionText = q.getQuestionText();
            item.optionA = q.getOptionA();
            item.optionB = q.getOptionB();
            item.optionC = q.getOptionC();
            item.optionD = q.getOptionD();
            item.selectedOption = a.selectedOption;
            item.correctOption = q.getCorrectOption(); // revealed only now, after submission
            item.isCorrect = isCorrect;
            review.add(item);
        }

        attempt.setAnswers(saved);
        attempt.setScore(correct);
        attempt.setEndTime(now);
        attempt.setStatus(autoSubmitted ? QuizAttempt.Status.AUTO_SUBMITTED : QuizAttempt.Status.SUBMITTED);
        attempt = attemptRepo.save(attempt);

        // Save completed set to Redis
        if (attempt.getSetNumber() != null) {
            String redisKey = COMPLETED_SETS_KEY_PREFIX + attempt.getUserId() + ":" + attempt.getSegmentKey();
            try {
                redis.setAdd(redisKey, attempt.getSetNumber());
            } catch (Exception e) {
                // Ignore Redis errors gracefully
            }
        }

        QuizResultResponse result = new QuizResultResponse();
        result.attemptId = attempt.getId();
        result.score = correct;
        result.totalQuestions = attempt.getTotalQuestions() != null ? attempt.getTotalQuestions() : req.answers.size();
        result.correctCount = correct;
        result.incorrectCount = result.totalQuestions - correct;
        result.percentage = result.totalQuestions == 0 ? 0
                : Math.round(correct * 10000.0 / result.totalQuestions) / 100.0;
        result.autoSubmitted = autoSubmitted;
        result.review = review;
        return result;
    }

    // ---------- Rotation countdown (Section 8.3) ----------

    public CountdownResponse getCountdown() {
        CountdownResponse resp = new CountdownResponse();
        resp.secondsToNextRotation = 0;
        resp.display = "Unlimited";
        resp.nextSlotTime = LocalDateTime.now();
        return resp;
    }

    /** Academic config rows are keyed "CLASS_{level}" — derived from "CLASS_10_MATH". */
    private String academicConfigKey(String segmentKey) {
        if (segmentKey.startsWith("CLASS_UNDER_5_") || segmentKey.equals("CLASS_UNDER_5")) return "CLASS_UNDER_5";
        if (segmentKey.startsWith("CLASS_6_TO_8_") || segmentKey.equals("CLASS_6_TO_8")) return "CLASS_6_TO_8";
        if (segmentKey.startsWith("CLASS_9_TO_12_") || segmentKey.equals("CLASS_9_TO_12")) return "CLASS_9_TO_12";
        String rest = segmentKey.substring("CLASS_".length());
        int underscore = rest.indexOf('_');
        return "CLASS_" + (underscore > 0 ? rest.substring(0, underscore) : rest);
    }
}
