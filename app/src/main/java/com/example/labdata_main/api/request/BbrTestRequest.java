package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 沥青弯曲蠕变劲度试验（弯曲梁流变仪法）请求模型
 */
public class BbrTestRequest {
    @SerializedName("taskId")
    private String taskId;
    
    @SerializedName("operatorId")
    private String operatorId;
    
    @SerializedName("specimenId")
    private String specimenId;
    
    @SerializedName("specimenType")
    private String specimenType;
    
    @SerializedName("materialType")
    private String materialType;
    
    @SerializedName("remarks")
    private String remarks;
    
    @SerializedName("experimentValues")
    private Map<String, String> experimentValues;
    
    public BbrTestRequest(String taskId, String operatorId, String specimenId, 
                       String specimenType, String materialType, 
                       String remarks, Map<String, String> experimentValues) {
        this.taskId = taskId;
        this.operatorId = operatorId;
        this.specimenId = specimenId;
        this.specimenType = specimenType;
        this.materialType = materialType;
        this.remarks = remarks;
        this.experimentValues = experimentValues;
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getOperatorId() {
        return operatorId;
    }
    
    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }
    
    public String getSpecimenId() {
        return specimenId;
    }
    
    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }
    
    public String getSpecimenType() {
        return specimenType;
    }
    
    public void setSpecimenType(String specimenType) {
        this.specimenType = specimenType;
    }
    
    public String getMaterialType() {
        return materialType;
    }
    
    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Map<String, String> getExperimentValues() {
        return experimentValues;
    }
    
    public void setExperimentValues(Map<String, String> experimentValues) {
        this.experimentValues = experimentValues;
    }
}
