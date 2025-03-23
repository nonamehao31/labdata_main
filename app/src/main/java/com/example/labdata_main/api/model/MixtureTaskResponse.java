package com.example.labdata_main.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * 混合料任务响应模型
 */
public class MixtureTaskResponse {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("taskId")
    private String taskId;
    
    @SerializedName("taskName")
    private String taskName;
    
    // 任务类型 - 混合料任务固定类型
    private String taskType = "MIXTURE";
    
    @SerializedName("taskCompany")
    private String taskCompany;
    
    @SerializedName("estBy")
    private Long estBy;
    
    @SerializedName("projectId")
    private String projectId;
    
    @SerializedName("projectName")
    private String projectName;
    
    @SerializedName("mixratioId")
    private Long mixratioId;
    
    @SerializedName("specimenId")
    private Long specimenId;
    
    @SerializedName("taskAssignment")
    private String taskAssignment;
    
    @SerializedName("remarks")
    private String remarks;
    
    @SerializedName("creationTime")
    private Long creationTime;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("testing_status")
    private String testingStatus;
    
    @SerializedName("dueDate")
    private String dueDate;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        // 如果没有任务名称，使用ID生成一个默认名称
        if (taskName == null || taskName.isEmpty()) {
            return "混合料任务-" + (id != null ? id : "未知");
        }
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    public String getTaskCompany() {
        return taskCompany;
    }
    
    public void setTaskCompany(String taskCompany) {
        this.taskCompany = taskCompany;
    }
    
    public Long getEstBy() {
        return estBy;
    }
    
    public void setEstBy(Long estBy) {
        this.estBy = estBy;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getProjectName() {
        // 如果没有项目名称，返回默认值
        return projectName != null ? projectName : "未知项目";
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public Long getMixratioId() {
        return mixratioId;
    }
    
    public void setMixratioId(Long mixratioId) {
        this.mixratioId = mixratioId;
    }
    
    public Long getSpecimenId() {
        return specimenId;
    }
    
    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }
    
    public String getTaskAssignment() {
        return taskAssignment;
    }
    
    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Long getCreationTime() {
        return creationTime;
    }
    
    public void setCreationTime(Long creationTime) {
        this.creationTime = creationTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTestingStatus() {
        return testingStatus;
    }
    
    public void setTestingStatus(String testingStatus) {
        this.testingStatus = testingStatus;
    }
    
    public String getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
