package com.smartgaon.ai.smartgaon_api.gaonsaathi.dto;

import java.util.List;

public class GaonSaathiChatResponse {
    private String reply;
    private String route;
    private String actionLabel;
    private List<String> suggestedChips;

    public GaonSaathiChatResponse() {}

    public GaonSaathiChatResponse(String reply, String route, String actionLabel, List<String> suggestedChips) {
        this.reply = reply;
        this.route = route;
        this.actionLabel = actionLabel;
        this.suggestedChips = suggestedChips;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public void setActionLabel(String actionLabel) {
        this.actionLabel = actionLabel;
    }

    public List<String> getSuggestedChips() {
        return suggestedChips;
    }

    public void setSuggestedChips(List<String> suggestedChips) {
        this.suggestedChips = suggestedChips;
    }
}
