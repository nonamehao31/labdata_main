package com.example.labdata.payload.response;

import com.example.labdata.model.AsphaltTask;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * 沥青实验响应体
 */
public class AsphaltExperimentResponse {
    private Long asphaltExperimentId;
    private String asphaltExperimentName;
    private String asphaltExperimentType;
    private String asphaltTaskName;
    private String asphaltTaskAssignment;
    private String asphaltTaskAssignmentId;
    private String taskStatus;
    private String status;
    private Long selectedAsphaltId;
    private String companyId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    public AsphaltExperimentResponse() {
    }

    public AsphaltExperimentResponse(AsphaltTask asphaltTask) {
        this.asphaltExperimentId = asphaltTask.getAsphaltExperimentId();
        this.asphaltExperimentName = asphaltTask.getAsphaltExperimentName();
        this.asphaltExperimentType = asphaltTask.getAsphaltExperimentType();
        this.asphaltTaskName = asphaltTask.getAsphaltTaskName();
        this.asphaltTaskAssignment = asphaltTask.getAsphaltTaskAssignment();
        this.asphaltTaskAssignmentId = asphaltTask.getAsphaltTaskAssignmentId();
        this.taskStatus = asphaltTask.getTaskStatus();
        this.status = asphaltTask.getStatus();
        this.selectedAsphaltId = asphaltTask.getSelectedAsphaltId();
        this.companyId = asphaltTask.getCompanyId();
        this.dueDate = asphaltTask.getDueDate();
    }

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

    public String getAsphaltTaskAssignmentId() {
        return asphaltTaskAssignmentId;
    }

    public void setAsphaltTaskAssignmentId(String asphaltTaskAssignmentId) {
        this.asphaltTaskAssignmentId = asphaltTaskAssignmentId;
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
    
    public LocalDate getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}
