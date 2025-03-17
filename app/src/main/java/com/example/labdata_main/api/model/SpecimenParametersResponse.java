package com.example.labdata_main.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * 制件参数响应模型
 */
public class SpecimenParametersResponse {
    
    @SerializedName("id")
    private Long id; // 制件方法ID
    
    @SerializedName("mixingTemperature")
    private Double mixingTemperature; // 拌合温度
    
    @SerializedName("mixingSpeed")
    private Double mixingSpeed; // 拌合速度
    
    @SerializedName("mixingTime")
    private Integer mixingTime; // 拌合时间
    
    @SerializedName("compactionMethod")
    private String compactionMethod; // 压实方法
    
    @SerializedName("mixRatioName")
    private String mixRatioName; // 添加配比名称

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMixingTemperature() {
        return mixingTemperature;
    }

    public void setMixingTemperature(Double mixingTemperature) {
        this.mixingTemperature = mixingTemperature;
    }

    public Double getMixingSpeed() {
        return mixingSpeed;
    }

    public void setMixingSpeed(Double mixingSpeed) {
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

    public String getMixRatioName() {
        return mixRatioName;
    }

    public void setMixRatioName(String mixRatioName) {
        this.mixRatioName = mixRatioName;
    }

    @Override
    public String toString() {
        return "SpecimenParametersResponse{" +
                "id=" + id +
                ", mixingTemperature=" + mixingTemperature +
                ", mixingSpeed=" + mixingSpeed +
                ", mixingTime=" + mixingTime +
                ", compactionMethod='" + compactionMethod + '\'' +
                ", mixRatioName='" + mixRatioName + '\'' +
                '}';
    }
}
