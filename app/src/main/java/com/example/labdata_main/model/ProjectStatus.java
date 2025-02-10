package com.example.labdata_main.model;

public class ProjectStatus {
    public static final String STATUS_NOT_STARTED = "未开始";
    public static final String STATUS_IN_PROGRESS = "进行中";
    public static final String STATUS_COMPLETED = "已完成";
    
    private String specimenStatus;
    private String experimentStatus;
    
    public ProjectStatus() {
        this.specimenStatus = STATUS_NOT_STARTED;
        this.experimentStatus = STATUS_NOT_STARTED;
    }
    
    public String getSpecimenStatus() {
        return specimenStatus;
    }
    
    public void setSpecimenStatus(String specimenStatus) {
        this.specimenStatus = specimenStatus;
    }
    
    public String getExperimentStatus() {
        return experimentStatus;
    }
    
    public void setExperimentStatus(String experimentStatus) {
        this.experimentStatus = experimentStatus;
    }
}
