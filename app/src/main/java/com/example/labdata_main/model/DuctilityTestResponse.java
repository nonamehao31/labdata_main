package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * 延度实验响应数据模型
 */
public class DuctilityTestResponse implements Serializable {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("taskId")
    private String taskId;
    
    @SerializedName("temperature") 
    private String temperature; // 温度
    
    @SerializedName("displacement") 
    private String displacement; // 拉长位移
    
    @SerializedName("experimenter")
    private String experimenter; // 实验人员
    
    @SerializedName("testDate")
    private Long testDate; // 实验日期
    
    @SerializedName("deviceId")
    private String deviceId; // 设备ID
    
    @SerializedName("deviceName")
    private String deviceName; // 设备名称
    
    @SerializedName("deviceManufacturer")
    private String deviceManufacturer; // 设备制造商
    
    @SerializedName("deviceModel")
    private String deviceModel; // 设备型号
    
    public DuctilityTestResponse() {
    }
    
    // Getters and setters
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
    
    public String getDisplacement() {
        return displacement;
    }
    
    public void setDisplacement(String displacement) {
        this.displacement = displacement;
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
}
