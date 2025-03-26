package com.example.labdata_main.model;

import java.time.LocalDateTime;

/**
 * 沥青弯曲蠕变劲度试验(弯曲梁流变仪法)响应模型
 */
public class BbrTestResponse {
    
    private Long id;
    
    // 基本信息
    private String taskId;                 // 任务ID
    private String operatorId;             // 操作员ID
    private String testDate;               // 测试日期
    private String specimenId;             // 试件ID
    private String specimenType;           // 试件类型
    private String materialType;           // 材料类型
    private String remarks;                // 备注
    
    // 试件尺寸数据
    private Double beamSpan;               // 弯曲梁跨度(mm)
    private Double specimenWidth;          // 试件宽度(mm)
    private Double specimenHeight;         // 试件高度(mm)
    
    // 8秒时间点数据
    private Double temperature8s;          // 8秒时的实验温度(°C)
    private Double load8s;                 // 8秒时的施加荷载(N)
    private Double deflection8s;           // 8秒时的变形挠度(mm)
    private Double stiffness8s;            // 8秒时的弯曲蠕变劲度模量(MPa)
    
    // 15秒时间点数据
    private Double temperature15s;         // 15秒时的实验温度(°C)
    private Double load15s;                // 15秒时的施加荷载(N)
    private Double deflection15s;          // 15秒时的变形挠度(mm)
    private Double stiffness15s;           // 15秒时的弯曲蠕变劲度模量(MPa)
    
    // 30秒时间点数据
    private Double temperature30s;         // 30秒时的实验温度(°C)
    private Double load30s;                // 30秒时的施加荷载(N)
    private Double deflection30s;          // 30秒时的变形挠度(mm)
    private Double stiffness30s;           // 30秒时的弯曲蠕变劲度模量(MPa)
    
    // 60秒时间点数据
    private Double temperature60s;         // 60秒时的实验温度(°C)
    private Double load60s;                // 60秒时的施加荷载(N)
    private Double deflection60s;          // 60秒时的变形挠度(mm)
    private Double stiffness60s;           // 60秒时的弯曲蠕变劲度模量(MPa)
    
    // 120秒时间点数据
    private Double temperature120s;        // 120秒时的实验温度(°C)
    private Double load120s;               // 120秒时的施加荷载(N)
    private Double deflection120s;         // 120秒时的变形挠度(mm)
    private Double stiffness120s;          // 120秒时的弯曲蠕变劲度模量(MPa)
    
    // 240秒时间点数据
    private Double temperature240s;        // 240秒时的实验温度(°C)
    private Double load240s;               // 240秒时的施加荷载(N)
    private Double deflection240s;         // 240秒时的变形挠度(mm)
    private Double stiffness240s;          // 240秒时的弯曲蠕变劲度模量(MPa)
    
    // 计算结果
    private Double creepRate;              // 蠕变速率(m值)
    
    // Getter和Setter方法
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

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
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

    public Double getBeamSpan() {
        return beamSpan;
    }

    public void setBeamSpan(Double beamSpan) {
        this.beamSpan = beamSpan;
    }

    public Double getSpecimenWidth() {
        return specimenWidth;
    }

    public void setSpecimenWidth(Double specimenWidth) {
        this.specimenWidth = specimenWidth;
    }

    public Double getSpecimenHeight() {
        return specimenHeight;
    }

    public void setSpecimenHeight(Double specimenHeight) {
        this.specimenHeight = specimenHeight;
    }

    public Double getTemperature8s() {
        return temperature8s;
    }

    public void setTemperature8s(Double temperature8s) {
        this.temperature8s = temperature8s;
    }

    public Double getLoad8s() {
        return load8s;
    }

    public void setLoad8s(Double load8s) {
        this.load8s = load8s;
    }

    public Double getDeflection8s() {
        return deflection8s;
    }

    public void setDeflection8s(Double deflection8s) {
        this.deflection8s = deflection8s;
    }

    public Double getStiffness8s() {
        return stiffness8s;
    }

    public void setStiffness8s(Double stiffness8s) {
        this.stiffness8s = stiffness8s;
    }

    public Double getTemperature15s() {
        return temperature15s;
    }

    public void setTemperature15s(Double temperature15s) {
        this.temperature15s = temperature15s;
    }

    public Double getLoad15s() {
        return load15s;
    }

    public void setLoad15s(Double load15s) {
        this.load15s = load15s;
    }

    public Double getDeflection15s() {
        return deflection15s;
    }

    public void setDeflection15s(Double deflection15s) {
        this.deflection15s = deflection15s;
    }

    public Double getStiffness15s() {
        return stiffness15s;
    }

    public void setStiffness15s(Double stiffness15s) {
        this.stiffness15s = stiffness15s;
    }

    public Double getTemperature30s() {
        return temperature30s;
    }

    public void setTemperature30s(Double temperature30s) {
        this.temperature30s = temperature30s;
    }

    public Double getLoad30s() {
        return load30s;
    }

    public void setLoad30s(Double load30s) {
        this.load30s = load30s;
    }

    public Double getDeflection30s() {
        return deflection30s;
    }

    public void setDeflection30s(Double deflection30s) {
        this.deflection30s = deflection30s;
    }

    public Double getStiffness30s() {
        return stiffness30s;
    }

    public void setStiffness30s(Double stiffness30s) {
        this.stiffness30s = stiffness30s;
    }

    public Double getTemperature60s() {
        return temperature60s;
    }

    public void setTemperature60s(Double temperature60s) {
        this.temperature60s = temperature60s;
    }

    public Double getLoad60s() {
        return load60s;
    }

    public void setLoad60s(Double load60s) {
        this.load60s = load60s;
    }

    public Double getDeflection60s() {
        return deflection60s;
    }

    public void setDeflection60s(Double deflection60s) {
        this.deflection60s = deflection60s;
    }

    public Double getStiffness60s() {
        return stiffness60s;
    }

    public void setStiffness60s(Double stiffness60s) {
        this.stiffness60s = stiffness60s;
    }

    public Double getTemperature120s() {
        return temperature120s;
    }

    public void setTemperature120s(Double temperature120s) {
        this.temperature120s = temperature120s;
    }

    public Double getLoad120s() {
        return load120s;
    }

    public void setLoad120s(Double load120s) {
        this.load120s = load120s;
    }

    public Double getDeflection120s() {
        return deflection120s;
    }

    public void setDeflection120s(Double deflection120s) {
        this.deflection120s = deflection120s;
    }

    public Double getStiffness120s() {
        return stiffness120s;
    }

    public void setStiffness120s(Double stiffness120s) {
        this.stiffness120s = stiffness120s;
    }

    public Double getTemperature240s() {
        return temperature240s;
    }

    public void setTemperature240s(Double temperature240s) {
        this.temperature240s = temperature240s;
    }

    public Double getLoad240s() {
        return load240s;
    }

    public void setLoad240s(Double load240s) {
        this.load240s = load240s;
    }

    public Double getDeflection240s() {
        return deflection240s;
    }

    public void setDeflection240s(Double deflection240s) {
        this.deflection240s = deflection240s;
    }

    public Double getStiffness240s() {
        return stiffness240s;
    }

    public void setStiffness240s(Double stiffness240s) {
        this.stiffness240s = stiffness240s;
    }

    public Double getCreepRate() {
        return creepRate;
    }

    public void setCreepRate(Double creepRate) {
        this.creepRate = creepRate;
    }
}
