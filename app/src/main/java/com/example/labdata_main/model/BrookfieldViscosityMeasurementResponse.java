package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 布鲁克菲尔德旋转黏度实验测量值响应类
 */
public class BrookfieldViscosityMeasurementResponse {
    @SerializedName("measurementId")
    private String measurementId;
    
    @SerializedName("spindleType")
    private String spindleType;
    
    @SerializedName("rotationSpeed")
    private String rotationSpeed;
    
    @SerializedName("viscosity")
    private String viscosity;
    
    public String getMeasurementId() {
        return measurementId;
    }
    
    public void setMeasurementId(String measurementId) {
        this.measurementId = measurementId;
    }
    
    public String getSpindleType() {
        return spindleType;
    }
    
    public void setSpindleType(String spindleType) {
        this.spindleType = spindleType;
    }
    
    public String getRotationSpeed() {
        return rotationSpeed;
    }
    
    public void setRotationSpeed(String rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }
    
    public String getViscosity() {
        return viscosity;
    }
    
    public void setViscosity(String viscosity) {
        this.viscosity = viscosity;
    }
}
