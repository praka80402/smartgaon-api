package com.smartgaon.ai.smartgaon_api.config;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final GroqQuestionService service;

    public QuizController(GroqQuestionService service) {
        this.service = service;
    }

    // ---------- RAW JSON RESPONSE ----------
    @GetMapping("/generate")
    public String generateQuiz(
            @RequestParam(defaultValue = "general knowledge") String category,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "en") String language
    ) {
        return service.generateQuestions(category, count, language);
    }

    // ---------- PARSED OBJECT RESPONSE ----------
    @GetMapping("/generate-structured")
    public List<QuizQuestion> generateQuizStructured(
            @RequestParam(defaultValue="general knowledge") String category,
            @RequestParam(defaultValue="5") int count,
            @RequestParam(defaultValue="en") String language
    ) throws Exception {
        return service.generateAndParseQuiz(category, count, language);
    }
}
