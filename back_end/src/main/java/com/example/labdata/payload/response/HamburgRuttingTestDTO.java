package com.example.labdata.payload.response;

import com.example.labdata.model.HamburgRuttingTest;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 汉堡车辙实验数据传输对象
 * 用于将后端数据模型转换为前端期望的格式
 */
public class HamburgRuttingTestDTO {
    
    private Long id;
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("mix_ratio_id")
    private Long mixRatioId;
    
    @JsonProperty("steady_slope1")
    private Float steadySlope1;
    
    @JsonProperty("steady_curvilinear1")
    private Float steadyCurvilinear1;
    
    @JsonProperty("steady_slope2")
    private Float steadySlope2;
    
    @JsonProperty("steady_curvilinear2")
    private Float steadyCurvilinear2;
    
    /**
     * 从实体对象转换为DTO
     */
    public static HamburgRuttingTestDTO fromEntity(HamburgRuttingTest entity) {
        HamburgRuttingTestDTO dto = new HamburgRuttingTestDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setMixRatioId(entity.getMixRatioId());
        dto.setSteadySlope1(entity.getSteadySlope1());
        dto.setSteadyCurvilinear1(entity.getSteadyCurvilinear1());
        dto.setSteadySlope2(entity.getSteadySlope2());
        dto.setSteadyCurvilinear2(entity.getSteadyCurvilinear2());
        return dto;
    }

    // Getters and Setters
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
