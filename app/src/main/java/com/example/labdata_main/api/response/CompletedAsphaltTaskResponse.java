package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 已完成沥青任务响应对象
 */
public class CompletedAsphaltTaskResponse {
    @SerializedName("asphalt_task_id")
    private String taskId;
    
    @SerializedName("asphalt_task_name")
    private String taskName;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("acceptor")
    private String acceptor;
    
    @SerializedName("accept_time")
    private String acceptTime;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;
    
    @SerializedName("asphalt_task_assignment")
    private String taskAssignment;
    
    @SerializedName("asphalt_experiment_id")
    private String experimentId;
    
    @SerializedName("asphalt_experiment_name")
    private String experimentName;
    
    @SerializedName("asphalt_experiment_type")
    private String experimentType;
    
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
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getTaskAssignment() {
        return taskAssignment;
    }
    
    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }
    
    public String getExperimentId() {
        return experimentId;
    }
    
    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }
    
    public String getExperimentName() {
        return experimentName;
    }
    
    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }
    
    public String getExperimentType() {
        return experimentType;
    }
    
    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }
}
