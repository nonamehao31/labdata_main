package com.example.labdata.payload.response;

import com.example.labdata.model.MarshallTest;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 马歇尔稳定度试验数据传输对象
 * 用于将后端数据模型转换为前端期望的格式
 */
public class MarshallTestDTO {
    
    private Long id;
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("stability_1")
    private Float stability1;
    
    @JsonProperty("stream_value1")
    private Float streamValue1;
    
    @JsonProperty("stability_2")
    private Float stability2;
    
    @JsonProperty("stream_value2")
    private Float streamValue2;
    
    @JsonProperty("stability_3")
    private Float stability3;
    
    @JsonProperty("stream_value3")
    private Float streamValue3;
    
    /**
     * 从实体对象转换为DTO
     */
    public static MarshallTestDTO fromEntity(MarshallTest entity) {
        MarshallTestDTO dto = new MarshallTestDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setStability1(entity.getStability1());
        dto.setStreamValue1(entity.getStreamValue1());
        dto.setStability2(entity.getStability2());
        dto.setStreamValue2(entity.getStreamValue2());
        dto.setStability3(entity.getStability3());
        dto.setStreamValue3(entity.getStreamValue3());
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

    public Float getStability1() {
        return stability1;
    }

    public void setStability1(Float stability1) {
        this.stability1 = stability1;
    }

    public Float getStreamValue1() {
        return streamValue1;
    }

    public void setStreamValue1(Float streamValue1) {
        this.streamValue1 = streamValue1;
    }

    public Float getStability2() {
        return stability2;
    }

    public void setStability2(Float stability2) {
        this.stability2 = stability2;
    }

    public Float getStreamValue2() {
        return streamValue2;
    }

    public void setStreamValue2(Float streamValue2) {
        this.streamValue2 = streamValue2;
    }

    public Float getStability3() {
        return stability3;
    }

    public void setStability3(Float stability3) {
        this.stability3 = stability3;
    }

    public Float getStreamValue3() {
        return streamValue3;
    }

    public void setStreamValue3(Float streamValue3) {
        this.streamValue3 = streamValue3;
    }
}
