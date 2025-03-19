package com.example.labdata_main.api.model;

/**
 * 试件制作方法响应模型
 * 用于接收后端返回的试件制作方法和配比信息
 */
public class SpecimenMethodResponse {
    
    private String mixName;
    private Float mixingTemperature;
    private Float mixingSpeed;
    private Integer mixingTime;
    private String compactionMethod;
    
    public SpecimenMethodResponse() {
    }
    
    public String getMixName() {
        return mixName;
    }
    
    public void setMixName(String mixName) {
        this.mixName = mixName;
    }
    
    public Float getMixingTemperature() {
        return mixingTemperature;
    }
    
    public void setMixingTemperature(Float mixingTemperature) {
        this.mixingTemperature = mixingTemperature;
    }
    
    public Float getMixingSpeed() {
        return mixingSpeed;
    }
    
    public void setMixingSpeed(Float mixingSpeed) {
        this.mixingSpeed = mixingSpeed;
    }
    
    public Integer getMixingTime() {
        return mixingTime;
    }
    
    public void setMixingTime(Integer mixingTime) {
        this.mixingTime = mixingTime;
    }
    
    public String getCompactionMethod() {
        return compactionMethod;
    }
    
    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }
    
    @Override
    public String toString() {
        return "SpecimenMethodResponse{" +
                "mixName='" + mixName + '\'' +
                ", mixingTemperature=" + mixingTemperature +
                ", mixingSpeed=" + mixingSpeed +
                ", mixingTime=" + mixingTime +
                ", compactionMethod='" + compactionMethod + '\'' +
                '}';
    }
}
