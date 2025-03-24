package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

/**
 * 动态剪切流变仪实验请求模型
 */
public class DynamicShearRheometerTestRequest {
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

    @SerializedName("controlMode")
    private String controlMode;

    @SerializedName("testRadius")
    private Double testRadius;

    @SerializedName("plateGap")
    private Double plateGap;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("experimentValues")
    private Map<String, String> experimentValues;

    public DynamicShearRheometerTestRequest(String taskId, String operatorId, String specimenId,
                                            String specimenType, String materialType, String controlMode,
                                            Double testRadius, Double plateGap, String remarks,
                                            Map<String, String> experimentValues) {
        this.taskId = taskId;
        this.operatorId = operatorId;
        this.specimenId = specimenId;
        this.specimenType = specimenType;
        this.materialType = materialType;
        this.controlMode = controlMode;
        this.testRadius = testRadius;
        this.plateGap = plateGap;
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

    public String getControlMode() {
        return controlMode;
    }

    public void setControlMode(String controlMode) {
        this.controlMode = controlMode;
    }

    public Double getTestRadius() {
        return testRadius;
    }

    public void setTestRadius(Double testRadius) {
        this.testRadius = testRadius;
    }

    public Double getPlateGap() {
        return plateGap;
    }

    public void setPlateGap(Double plateGap) {
        this.plateGap = plateGap;
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