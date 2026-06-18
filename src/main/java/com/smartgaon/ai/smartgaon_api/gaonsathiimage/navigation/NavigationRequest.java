package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation;

public class NavigationRequest {

    private String message;

    public NavigationRequest() {
    }

    public NavigationRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}