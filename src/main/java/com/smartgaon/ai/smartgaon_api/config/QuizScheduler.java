package com.smartgaon.ai.smartgaon_api.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;
import java.util.Random;

@Configuration
@EnableScheduling
public class QuizScheduler {

    private final GroqQuestionService service;
    private final QuizCache cache;

    private final List<String> categories = List.of(
            "teacher",
            "farmer",
            "doctor",
            "student",
            "general knowledge"
    );

    private final Random random = new Random();

    public QuizScheduler(GroqQuestionService service, QuizCache cache) {
        this.service = service;
        this.cache = cache;
    }

    // Run every 4 hours → 00:00, 04:00, 08:00, 12:00, 16:00, 20:00
    @Scheduled(cron = "0 0 */4 * * *")
    public void generateRandomCategoryQuiz() throws Exception {
        String category = categories.get(random.nextInt(categories.size()));
        String language = "en"; // or "hi" / "mr" if you want random later
        int count = 5;

        var quiz = service.generateAndParseQuiz(category, count, language);
        cache.setQuiz(quiz);

        System.out.println("🔄 New quiz generated | Category: " + category + " | Questions: " + quiz.size());
    }
}
