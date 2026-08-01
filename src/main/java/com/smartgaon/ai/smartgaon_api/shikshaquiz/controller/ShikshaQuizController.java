package com.smartgaon.ai.smartgaon_api.shikshaquiz.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuizDtos.SubmitQuizRequest;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.service.PerformanceService;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.service.QuizService;

/**
 * User-facing quiz endpoints for the website.
 *
 * Base path is /api/shiksha-quiz (the old Groq quiz owns /api/quiz).
 * userId comes from a request param (like the rest of this app, which is
 * token-free and identifies users by id/phone), not from a JWT.
 */
@RestController
@RequestMapping("/api/shiksha-quiz")
public class ShikshaQuizController {

    private final QuizService quizService;
    private final PerformanceService performanceService;

    public ShikshaQuizController(QuizService quizService,
                                 PerformanceService performanceService) {
        this.quizService = quizService;
        this.performanceService = performanceService;
    }

    // ---------- Start quiz ----------
    @PostMapping("/start")
    public ResponseEntity<?> start(@RequestParam Long userId,
                                   @RequestParam String segmentKey,
                                   @RequestParam(required = false, defaultValue = "EN") String language) {
        return ResponseEntity.ok(quizService.startQuiz(userId, segmentKey, language));
    }

    // ---------- One-time extra time ----------
    @PostMapping("/attempt/{attemptId}/extra-time")
    public ResponseEntity<?> extraTime(@PathVariable Long attemptId) {
        return ResponseEntity.ok(quizService.grantExtraTime(attemptId));
    }

    // ---------- Submit ----------
    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody SubmitQuizRequest req) {
        return ResponseEntity.ok(quizService.submitQuiz(req));
    }

    // ---------- Performance dashboard (last 5) ----------
    @GetMapping("/performance")
    public ResponseEntity<?> performance(@RequestParam Long userId) {
        return ResponseEntity.ok(performanceService.getPerformance(userId));
    }

    // ---------- Rotation countdown ----------
    @GetMapping("/rotation/countdown")
    public ResponseEntity<?> countdown() {
        return ResponseEntity.ok(quizService.getCountdown());
    }
}
