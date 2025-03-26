package com.example.labdata.payload.dto;

import java.util.List;

/**
 * DSR实验结果DTO
 */
public class DsrTestResult {
    private Long id;
    private String taskId;
    private String operatorId;
    private String specimenId;
    private String specimenType;
    private String materialType;
    private String controlMode;
    private Double testRadius;
    private Double plateGap;
    private String remarks;
    private List<DsrDataPoint> dataPoints;

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

    public List<DsrDataPoint> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(List<DsrDataPoint> dataPoints) {
        this.dataPoints = dataPoints;
    }
}
