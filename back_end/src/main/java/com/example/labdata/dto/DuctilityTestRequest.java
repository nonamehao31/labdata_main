package com.example.labdata.dto;

import lombok.Data;

/**
 * 延度实验数据请求DTO
 */
@Data
public class DuctilityTestRequest {
    private String taskId; // 使用String类型而非Long，避免大数值问题
    private String temperature;
    private String displacement;
    private String experimenter;
    private Long testDate;
    private String deviceId;
    private String deviceName;
    private String deviceManufacturer;
    private String deviceModel;
}
