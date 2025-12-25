package com.smartgaon.ai.smartgaon_api.config;


import java.util.List;

public class QuizQuestion {
    private String question;
    private List<String> options;
    private String answer;

    // getters & setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
}
