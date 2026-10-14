package com.smartgaon.ai.smartgaon_api.gaonsaathi.dto;

public class GaonSaathiChatRequest {
    private String question;
    private String language;
    private String userLocation;

    public GaonSaathiChatRequest() {}

    public GaonSaathiChatRequest(String question, String language, String userLocation) {
        this.question = question;
        this.language = language;
        this.userLocation = userLocation;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
}
