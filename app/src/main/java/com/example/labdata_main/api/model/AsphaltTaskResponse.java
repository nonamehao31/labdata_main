package com.example.labdata_main.api.model;

import java.time.Instant;

/**
 * 沥青实验任务响应模型 - 与后端AsphaltExperimentResponse对应
 */
public class AsphaltTaskResponse {
    
    // 注意：这里不使用@SerializedName是因为我们期望Gson直接使用Java属性名映射JSON字段
    // 后端响应中的字段名与这些属性名完全相同（驼峰式命名）
    
    private Long asphaltExperimentId;
    private String asphaltExperimentName;
    private String asphaltExperimentType;
    private String asphaltTaskName;
    private String asphaltTaskAssignment;
    private String taskStatus;
    private String status;
    private Long selectedAsphaltId;
    private String companyId;
    private String createdAt;
    private String updatedAt;
    
    // Getters and Setters
    public Long getAsphaltExperimentId() {
        return asphaltExperimentId;
    }
    
    public void setAsphaltExperimentId(Long asphaltExperimentId) {
        this.asphaltExperimentId = asphaltExperimentId;
    }
    
    public String getAsphaltExperimentName() {
        return asphaltExperimentName;
    }
    
    public void setAsphaltExperimentName(String asphaltExperimentName) {
        this.asphaltExperimentName = asphaltExperimentName;
    }
    
    public String getAsphaltExperimentType() {
        return asphaltExperimentType;
    }
    
    public void setAsphaltExperimentType(String asphaltExperimentType) {
        this.asphaltExperimentType = asphaltExperimentType;
    }
    
    public String getAsphaltTaskName() {
        return asphaltTaskName;
    }
    
    public void setAsphaltTaskName(String asphaltTaskName) {
        this.asphaltTaskName = asphaltTaskName;
    }
    
    public String getAsphaltTaskAssignment() {
        return asphaltTaskAssignment;
    }
    
    public void setAsphaltTaskAssignment(String asphaltTaskAssignment) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
    }
    
    public String getTaskStatus() {
        return taskStatus;
    }
    
    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getSelectedAsphaltId() {
        return selectedAsphaltId;
    }
    
    public void setSelectedAsphaltId(Long selectedAsphaltId) {
        this.selectedAsphaltId = selectedAsphaltId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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
}
