package com.example.labdata_main.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * 制件参数响应模型
 */
public class SpecimenParametersResponse {
    
    @SerializedName("id")
    private Long id; // 制件方法ID
    
    @SerializedName("mixingTemperature")
    private Float mixingTemperature; // 拌合温度
    
    @SerializedName("mixingSpeed")
    private Float mixingSpeed; // 拌合速度
    
    @SerializedName("mixingTime")
    private Integer mixingTime; // 拌合时间
    
    @SerializedName("compactionMethod")
    private String compactionMethod; // 压实方法

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        return "SpecimenParametersResponse{" +
                "id=" + id +
                ", mixingTemperature=" + mixingTemperature +
                ", mixingSpeed=" + mixingSpeed +
                ", mixingTime=" + mixingTime +
                ", compactionMethod='" + compactionMethod + '\'' +
                '}';
    }
}
