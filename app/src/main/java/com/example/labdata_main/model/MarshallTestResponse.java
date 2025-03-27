package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 马歇尔稳定度实验数据响应类
 */
public class MarshallTestResponse {
    @SerializedName("id")
    private Long id;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("stability_1")
    private Float stability1;

    @SerializedName("stream_value1")
    private Float streamValue1;

    @SerializedName("stability_2")
    private Float stability2;

    @SerializedName("stream_value2")
    private Float streamValue2;

    @SerializedName("stability_3")
    private Float stability3;

    @SerializedName("stream_value3")
    private Float streamValue3;

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
