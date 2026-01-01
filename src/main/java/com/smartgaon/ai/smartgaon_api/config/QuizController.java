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

    @GetMapping("/guide")
    public GroqQuestionService.CareerGuideResponse guide(
            @RequestParam String field,
            @RequestParam(defaultValue = "0") int exp,
            @RequestParam(defaultValue = "en") String lang
    ) throws Exception {
        return service.generateCareerGuide(field, exp, lang);
    }


}
