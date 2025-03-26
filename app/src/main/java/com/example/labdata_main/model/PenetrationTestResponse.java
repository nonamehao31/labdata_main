package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * 针入度实验数据响应模型
 */
public class PenetrationTestResponse implements Serializable {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("temperature")
    private String temperature;
    
    @SerializedName("reading")
    private String reading;
    
    @SerializedName("device_name")
    private String deviceName;
    
    @SerializedName("device_manufacturer")
    private String deviceManufacturer;
    
    @SerializedName("device_model")
    private String deviceModel;
    
    public PenetrationTestResponse() {
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
    
    public String getTemperature() {
        return temperature;
    }
    
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    
    public String getReading() {
        return reading;
    }
    
    public void setReading(String reading) {
        this.reading = reading;
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
