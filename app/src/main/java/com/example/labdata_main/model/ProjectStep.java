package com.example.labdata_main.model;

public class ProjectStep {
    private int stepNumber;
    private String stepName;
    private String status;
    private long completionTime;
    private String actionButtonText;
    private String statusInfo;

    public ProjectStep(int stepNumber, String stepName) {
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.status = "pending"; // pending, in_progress, completed
        this.completionTime = 0;
        this.actionButtonText = "开始";
        this.statusInfo = "";
    }

    // Getters and Setters
    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }

    public String getActionButtonText() {
        return actionButtonText;
    }

    public void setActionButtonText(String actionButtonText) {
        this.actionButtonText = actionButtonText;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }
}
