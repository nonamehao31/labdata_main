package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验响应类
 */
public class BrookfieldViscosityResponse {
    @SerializedName("id")
    private Long id;
    
    @SerializedName("taskId")
    private String taskId;
    
    @SerializedName("deviceId")
    private String deviceId;
    
    @SerializedName("deviceName")
    private String deviceName;
    
    @SerializedName("deviceManufacturer")
    private String deviceManufacturer;
    
    @SerializedName("deviceModel")
    private String deviceModel;
    
    @SerializedName("temperaturePoints")
    private List<BrookfieldViscosityTemperaturePointResponse> temperaturePoints;
    
    public BrookfieldViscosityResponse() {
        temperaturePoints = new ArrayList<>();
    }
    
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
    
    public List<BrookfieldViscosityTemperaturePointResponse> getTemperaturePoints() {
        return temperaturePoints;
    }
    
    public void setTemperaturePoints(List<BrookfieldViscosityTemperaturePointResponse> temperaturePoints) {
        this.temperaturePoints = temperaturePoints;
    }
}
