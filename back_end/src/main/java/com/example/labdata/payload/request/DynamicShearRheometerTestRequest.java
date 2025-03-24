package com.example.labdata.payload.request;

import lombok.Data;
import java.util.Map;

/**
 * 动态剪切流变仪实验请求模型
 */
@Data
public class DynamicShearRheometerTestRequest {
    private String taskId;
    private String operatorId;
    private String specimenId;
    private String specimenType;
    private String materialType;
    private String controlMode;
    private Double testRadius;
    private Double plateGap;
    private String remarks;
    private Map<String, String> experimentValues;
}