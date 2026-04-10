package com.smartgaon.ai.smartgaon_api.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final GroqQuestionService service;

    public QuizController(GroqQuestionService service) {
        this.service = service;
    }

    @GetMapping("/groq-key")
    public GroqQuestionService.GroqKeyInfo groqKeyInfo() {
        return service.getGroqKeyInfo();
    }

    @PostMapping("/groq-key/raw")
    public Map<String, String> rawGroqKey(@RequestBody GroqKeyRequest request) {
        if (request == null || !"-9321003831".equals(request.key())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid key payload");
        }
        return Map.of("groqKey", service.getRawGroqApiKey());
    }

    // ---------- PARSED OBJECT RESPONSE ----------
    @GetMapping("/generate")
    public List<QuizQuestion> generateQuizStructured(
            @RequestParam(defaultValue="general knowledge") String category,
            @RequestParam(defaultValue="10") int count,
            @RequestParam(defaultValue="en") String language
    ) throws Exception {
        return service.generateAndParseQuiz(category, count, language);
    }
    @GetMapping("/class-quiz")
    public List<QuizQuestion> generateClassQuiz(
            @RequestParam int classGrade,
            @RequestParam String subject,
            @RequestParam(defaultValue="10") int count,
            @RequestParam(defaultValue="en") String language
    ) throws Exception {
        String response = service.generateQuestionsForClassAndSubject(classGrade, subject, count, language);
        return service.extractQuestions(response);
    }
    @GetMapping("/govt")
    public List<QuizQuestion> generateGovtQuiz(
            @RequestParam(defaultValue="10") int count,
            @RequestParam(defaultValue="en") String lang,
            @RequestParam(defaultValue="SSC") String examType
    ) throws Exception {
        String raw = service.generateGovtExamDailyQuiz(count, lang, examType);
        return service.extractQuestions(raw);
    }



    public record GroqKeyRequest(String key) {}
}
