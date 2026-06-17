package com.smartgaon.ai.smartgaon_api.gaonsathiimage.navigation;

public class NavigationResponse {

    private boolean success;
    private String moduleCode;
    private String moduleName;
    private String routePath;
    private String matchedAlias;
    private Integer confidence;
    private String message;

    public NavigationResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getMatchedAlias() {
        return matchedAlias;
    }

    public void setMatchedAlias(String matchedAlias) {
        this.matchedAlias = matchedAlias;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}