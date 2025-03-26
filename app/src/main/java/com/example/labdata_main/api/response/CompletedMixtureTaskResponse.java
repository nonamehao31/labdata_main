package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * 已完成混合料任务响应对象
 */
public class CompletedMixtureTaskResponse {
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("task_name")
    private String taskName;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("acceptor")
    private String acceptor;
    
    @SerializedName("accept_time")
    private String acceptTime;
    
    @SerializedName("creation_time")
    private String creationTime;
    
    @SerializedName("mixratio_id")
    private String mixratioId;
    
    @SerializedName("mix_name")
    private String mixName;
    
    @SerializedName("specimen_id")
    private String specimenId;
    
    @SerializedName("compaction_method")
    private String compactionMethod;
    
    @SerializedName("task_assignment")
    private String taskAssignment;
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAcceptor() {
        return acceptor;
    }
    
    public void setAcceptor(String acceptor) {
        this.acceptor = acceptor;
    }
    
    public String getAcceptTime() {
        return acceptTime;
    }
    
    public void setAcceptTime(String acceptTime) {
        this.acceptTime = acceptTime;
    }
    
    public String getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(String creationTime) {
        this.creationTime = creationTime;
    }
    
    public String getMixratioId() {
        return mixratioId;
    }
    
    public void setMixratioId(String mixratioId) {
        this.mixratioId = mixratioId;
    }
    
    public String getMixName() {
        return mixName;
    }
    
    public void setMixName(String mixName) {
        this.mixName = mixName;
    }
    
    public String getSpecimenId() {
        return specimenId;
    }
    
    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }
    
    public String getCompactionMethod() {
        return compactionMethod;
    }
    
    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }
    
    public String getTaskAssignment() {
        return taskAssignment;
    }
    
    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }
}
