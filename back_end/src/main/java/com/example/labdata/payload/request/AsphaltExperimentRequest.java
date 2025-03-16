package com.example.labdata.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 沥青实验请求参数
 */
public class AsphaltExperimentRequest {

    @NotBlank(message = "实验名称不能为空")
    @Size(max = 100, message = "实验名称长度不能超过100个字符")
    @JsonProperty("asphalt_experiment_name")
    private String asphaltExperimentName;

    @NotBlank(message = "实验类型不能为空")
    @Size(max = 20, message = "实验类型长度不能超过20个字符")
    @JsonProperty("asphalt_experiment_type")
    private String asphaltExperimentType;

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    @JsonProperty("asphalt_task_name")
    private String asphaltTaskName;

    @JsonProperty("asphalt_task_assignment")
    private String asphaltTaskAssignment;

    @JsonProperty("asphalt_task_assignment_id")
    private String asphaltTaskAssignmentId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("task_status")
    private String taskStatus;

    @JsonProperty("selected_asphalt_id")
    private Long selectedAsphaltId;

    @JsonProperty("company_id")
    private String companyId;

    public AsphaltExperimentRequest() {
    }

    public AsphaltExperimentRequest(String asphaltExperimentName, String asphaltExperimentType) {
        this.asphaltExperimentName = asphaltExperimentName;
        this.asphaltExperimentType = asphaltExperimentType;
    }

    public AsphaltExperimentRequest(String asphaltExperimentName, String asphaltExperimentType, String asphaltTaskName) {
        this.asphaltExperimentName = asphaltExperimentName;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
    }

    public AsphaltExperimentRequest(String asphaltExperimentName, String asphaltExperimentType, String asphaltTaskName, String asphaltTaskAssignment) {
        this.asphaltExperimentName = asphaltExperimentName;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
        this.asphaltTaskAssignment = asphaltTaskAssignment;
    }

    public AsphaltExperimentRequest(String asphaltExperimentName, String asphaltExperimentType, String asphaltTaskName, String asphaltTaskAssignment, String status) {
        this.asphaltExperimentName = asphaltExperimentName;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.status = status;
        this.taskStatus = "CREATED";
    }

    public AsphaltExperimentRequest(String asphaltExperimentName, String asphaltExperimentType, String asphaltTaskName, String asphaltTaskAssignment, String status, String taskStatus) {
        this.asphaltExperimentName = asphaltExperimentName;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.status = status;
        this.taskStatus = taskStatus;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
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
}
