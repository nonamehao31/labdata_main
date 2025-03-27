package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 汉堡车辙实验数据响应类
 */
public class HamburgRuttingTestResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("mix_ratio_id")
    private Long mixRatioId;

    @SerializedName("steady_slope1")
    private Float steadySlope1;

    @SerializedName("steady_curvilinear1")
    private Float steadyCurvilinear1;

    @SerializedName("steady_slope2")
    private Float steadySlope2;

    @SerializedName("steady_curvilinear2")
    private Float steadyCurvilinear2;

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
    
    public Long getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(Long mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public Float getSteadySlope1() {
        return steadySlope1;
    }

    public void setSteadySlope1(Float steadySlope1) {
        this.steadySlope1 = steadySlope1;
    }

    public Float getSteadyCurvilinear1() {
        return steadyCurvilinear1;
    }

    public void setSteadyCurvilinear1(Float steadyCurvilinear1) {
        this.steadyCurvilinear1 = steadyCurvilinear1;
    }

    public Float getSteadySlope2() {
        return steadySlope2;
    }

    public void setSteadySlope2(Float steadySlope2) {
        this.steadySlope2 = steadySlope2;
    }

    public Float getSteadyCurvilinear2() {
        return steadyCurvilinear2;
    }

    public void setSteadyCurvilinear2(Float steadyCurvilinear2) {
        this.steadyCurvilinear2 = steadyCurvilinear2;
    }
}
