package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验温度点响应类
 */
public class BrookfieldViscosityTemperaturePointResponse {
    @SerializedName("pointId")
    private String pointId;
    
    @SerializedName("temperature")
    private String temperature;
    
    @SerializedName("measurements")
    private List<BrookfieldViscosityMeasurementResponse> measurements;
    
    public BrookfieldViscosityTemperaturePointResponse() {
        measurements = new ArrayList<>();
    }
    
    public String getPointId() {
        return pointId;
    }
    
    public void setPointId(String pointId) {
        this.pointId = pointId;
    }
    
    public String getTemperature() {
        return temperature;
    }
    
    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
    
    public List<BrookfieldViscosityMeasurementResponse> getMeasurements() {
        return measurements;
    }
    
    public void setMeasurements(List<BrookfieldViscosityMeasurementResponse> measurements) {
        this.measurements = measurements;
    }
}
