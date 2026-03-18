package com.smartgaon.ai.smartgaon_api.QA.model;

import jakarta.persistence.*;

@Entity
@Table(name = "qa_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = "question")
})
public class QA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 500, nullable = false, unique = true)
    private String question;

    @Column(columnDefinition = "TEXT")
    private String answer;

    // GETTERS & SETTERS

    public Long getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}