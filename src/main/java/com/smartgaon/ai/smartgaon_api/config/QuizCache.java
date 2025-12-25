package com.smartgaon.ai.smartgaon_api.config;


import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class QuizCache {
    private List<QuizQuestion> latestQuiz;

    public void setQuiz(List<QuizQuestion> quiz) {
        this.latestQuiz = quiz;
    }

    public List<QuizQuestion> getQuiz() {
        return latestQuiz;
    }
}
