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
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuestionBatch;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuizAttempt;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.SegmentType;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.CompetitionConfigRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuestionRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuizAttemptRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.util.QuizTimeUtil;

/**
 * User-facing quiz flow: start -> (optional extra time) -> batched submit + scoring
 * (Sections 8 & 9), attempt limits on the 5 AM quiz-day (Section 8.2), and the
 * server-side timer validation that never trusts the client clock (Section 8.1).
 */
@Service
public class QuizService {

    public static final int DEFAULT_COMPETITION_ATTEMPT_LIMIT = 2;
    public static final String ATTEMPT_COUNT_KEY_PREFIX = "quiz:attempt_count:";
    /** grace period for network latency when validating elapsed time */
    public static final int SUBMIT_GRACE_SECONDS = 30;

    @Autowired private QuizAttemptRepository attemptRepo;
    @Autowired private QuestionRepository questionRepo;
    @Autowired private CompetitionConfigRepository configRepo;
    @Autowired private RotationSchedulerService rotationService;
    @Autowired private QuizRedisService redis;

    // ---------- Start quiz ----------

    @Transactional
    public Object startQuiz(Long userId, String segmentKey) {
        LocalDateTime now = LocalDateTime.now();
        SegmentType segmentType = segmentKey.startsWith("CLASS_")
                ? SegmentType.ACADEMIC : SegmentType.COMPETITION;
        String competitionType = segmentType == SegmentType.COMPETITION ? segmentKey : null;

        CompetitionConfig config = configRepo.findByCompetitionType(
                segmentType == SegmentType.COMPETITION ? competitionType : academicConfigKey(segmentKey))
                .orElse(null);

        // ----- Daily attempt limit: 2 per competition per quiz-day (Section 8.2) -----
        if (segmentType == SegmentType.COMPETITION) {
            int limit = config != null && config.getDailyAttemptLimit() != null
                    ? config.getDailyAttemptLimit() : DEFAULT_COMPETITION_ATTEMPT_LIMIT;

            String counterKey = ATTEMPT_COUNT_KEY_PREFIX + userId + ":" + competitionType
                    + ":" + QuizTimeUtil.getQuizDay(now);

            Long count = redis.increment(counterKey, QuizTimeUtil.secondsToNextReset(now));
            if (count == null) {
                // Redis unavailable -> DB fallback (counts completed + in-progress today)
                count = attemptRepo.countByUserIdAndCompetitionTypeAndQuizDay(
                        userId, competitionType, QuizTimeUtil.getQuizDay(now)) + 1;
            }

            if (count > limit) {
                redis.decrement(counterKey); // undo our INCR (no-op if Redis was unavailable)
                AttemptLimitResponse limitResp = new AttemptLimitResponse();
                limitResp.nextRotationIn = QuizTimeUtil.formatCountdown(now);
                limitResp.secondsToNextRotation = QuizTimeUtil.secondsToNextSlot(now);
                limitResp.message = "You've completed your " + limit
                        + " attempts for this competition today. Try a different competition, "
                        + "or a new question set will be available in " + limitResp.nextRotationIn + ".";
                return limitResp;
            }
        }

        // ----- Active batch: Redis first, DB fallback (Section 11.1) -----
        QuestionBatch batch = rotationService.getActiveBatch(segmentKey);
        if (batch == null || batch.getQuestionIds().isEmpty()) {
            throw new IllegalStateException("No active question batch for segment: " + segmentKey);
        }

        List<Question> questions = questionRepo.findAllById(batch.getQuestionIds());
        // deleted questions are excluded automatically going forward
        questions.removeIf(q -> !q.isActive());

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setSegmentKey(segmentKey);
        attempt.setSegmentType(segmentType);
        attempt.setCompetitionType(competitionType);
        attempt.setStartTime(now);
        attempt.setQuizDay(QuizTimeUtil.getQuizDay(now));
        attempt.setTotalQuestions(questions.size());
        attempt = attemptRepo.save(attempt);

        StartQuizResponse resp = new StartQuizResponse();
        resp.attemptId = attempt.getId();
        resp.segmentKey = segmentKey;
        resp.secondsToNextRotation = QuizTimeUtil.secondsToNextSlot(now);
        // correctOption is NEVER sent to the frontend
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
        attemptRepo.save(attempt);

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
        LocalDateTime now = LocalDateTime.now();
        CountdownResponse resp = new CountdownResponse();
        resp.secondsToNextRotation = QuizTimeUtil.secondsToNextSlot(now);
        resp.display = QuizTimeUtil.formatCountdown(now);
        resp.nextSlotTime = QuizTimeUtil.nextSlotTime(now);
        return resp;
    }

    /** Academic config rows are keyed "CLASS_{level}" — derived from "CLASS_10_MATH". */
    private String academicConfigKey(String segmentKey) {
        String rest = segmentKey.substring("CLASS_".length());
        int underscore = rest.indexOf('_');
        return "CLASS_" + (underscore > 0 ? rest.substring(0, underscore) : rest);
    }
}
