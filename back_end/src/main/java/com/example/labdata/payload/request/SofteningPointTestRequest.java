package com.example.labdata.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 软化点试验请求DTO
 */
@Data
public class SofteningPointTestRequest {
    
    /**
     * 任务ID - 使用String类型避免大数值问题
     */
    @JsonProperty("task_id")
    private String taskId;
    
    /**
     * 初始温度值
     */
    @JsonProperty("temperature")
    private String temperature;
    
    /**
     * 软化温度值
     */
    @JsonProperty("softening_temperature")
    private String softeningTemperature;
    
    /**
     * 试验操作人
     */
    @JsonProperty("experimenter")
    private String experimenter;
    
    /**
     * 试验日期(毫秒时间戳)
     */
    @JsonProperty("test_date")
    private Long testDate;
    
    /**
     * 设备ID
     */
    @JsonProperty("device_id")
    private String deviceId;
    
    /**
     * 设备名称
     */
    @JsonProperty("device_name")
    private String deviceName;
    
    /**
     * 设备制造商
     */
    @JsonProperty("device_manufacturer")
    private String deviceManufacturer;
    
    /**
     * 设备型号
     */
    @JsonProperty("device_model")
    private String deviceModel;
}
