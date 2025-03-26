package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 软化点试验（环球法）响应数据模型
 */
public class SofteningPointResponse implements Serializable {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("taskId")
    private String taskId;
    
    @SerializedName("temperature")
    private String temperature;
    
    @SerializedName("softeningTemperature")
    private String softeningTemperature;
    
    @SerializedName("experimenter")
    private String experimenter;
    
    @SerializedName("testDate")
    private Long testDate;
    
    @SerializedName("deviceId")
    private String deviceId;
    
    @SerializedName("deviceName")
    private String deviceName;
    
    @SerializedName("deviceManufacturer")
    private String deviceManufacturer;
    
    @SerializedName("deviceModel")
    private String deviceModel;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
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
    
    public String getTemperature() {
        return temperature;
    }
    
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    
    public String getSofteningTemperature() {
        return softeningTemperature;
    }
    
    public void setSofteningTemperature(String softeningTemperature) {
        this.softeningTemperature = softeningTemperature;
    }
    
    public String getExperimenter() {
        return experimenter;
    }
    
    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
    }
    
    public Long getTestDate() {
        return testDate;
    }
    
    public void setTestDate(Long testDate) {
        this.testDate = testDate;
    }
    
    public String getDeviceId() {
        return deviceId;
    }
    
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public String getDeviceName() {
        return deviceName;
    }
    
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    
    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }
    
    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }
    
    public String getDeviceModel() {
        return deviceModel;
    }
    
    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
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
