package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 沥青实验请求体
 */
public class AsphaltExperimentRequest {
    @SerializedName("asphalt_task_assignment")
    private String asphaltTaskAssignment;
    
    @SerializedName("asphalt_experiment_type")
    private String asphaltExperimentType;
    
    @SerializedName("asphalt_task_name")
    private String asphaltTaskName;
    
    @SerializedName("task_status")
    private String taskStatus;
    
    @SerializedName("selected_asphalt_id")
    private Long selectedAsphaltId;
    
    @SerializedName("company_id")
    private String companyId;

    public AsphaltExperimentRequest(String asphaltTaskAssignment) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.asphaltExperimentType = "ASPHALT"; // 默认为ASPHALT类型
    }
    
    public AsphaltExperimentRequest(String asphaltTaskAssignment, String asphaltTaskName) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.asphaltExperimentType = "ASPHALT"; // 默认为ASPHALT类型
        this.asphaltTaskName = asphaltTaskName;
    }
    
    public AsphaltExperimentRequest(String asphaltTaskAssignment, String asphaltExperimentType, 
                                   String asphaltTaskName) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
    }
    
    public AsphaltExperimentRequest(String asphaltTaskAssignment, String asphaltExperimentType, 
                                   String asphaltTaskName, String taskStatus) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
        this.asphaltExperimentType = asphaltExperimentType;
        this.asphaltTaskName = asphaltTaskName;
        this.taskStatus = taskStatus;
    }

    public String getAsphaltTaskAssignment() {
        return asphaltTaskAssignment;
    }

    public void setAsphaltTaskAssignment(String asphaltTaskAssignment) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
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
    
    /**
     * 向后兼容 - 用于处理旧代码调用的方法
     * @deprecated 使用getAsphaltTaskAssignment代替
     */
    @Deprecated
    public String getAsphaltExperimentName() {
        return asphaltTaskAssignment;
    }
    
    /**
     * 向后兼容 - 用于处理旧代码调用的方法
     * @deprecated 使用setAsphaltTaskAssignment代替
     */
    @Deprecated
    public void setAsphaltExperimentName(String asphaltExperimentName) {
        this.asphaltTaskAssignment = asphaltExperimentName;
    }
}
