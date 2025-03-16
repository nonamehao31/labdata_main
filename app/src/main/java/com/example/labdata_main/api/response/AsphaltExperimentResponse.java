package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青实验响应体
 */
public class AsphaltExperimentResponse {
    @SerializedName("asphalt_experiment_id")
    private Long asphaltExperimentId;
    
    @SerializedName("asphalt_experiment_name")
    private String asphaltExperimentName;
    
    @SerializedName("asphalt_experiment_type")
    private String asphaltExperimentType;
    
    @SerializedName("asphalt_task_name")
    private String asphaltTaskName;
    
    @SerializedName("asphalt_task_assignment")
    private String asphaltTaskAssignment;
    
    @SerializedName("task_status")
    private String taskStatus;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("selected_asphalt_id")
    private Long selectedAsphaltId;
    
    @SerializedName("company_id")
    private String companyId;

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
}
