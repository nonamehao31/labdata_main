package com.example.labdata.payload.request;

import lombok.Data;
import java.util.Map;

/**
 * 沥青弯曲蠕变劲度试验（弯曲梁流变仪法）请求模型
 */
@Data
public class BbrTestRequest {
    private String taskId;             // 任务ID
    private String operatorId;         // 操作员ID
    private String specimenId;         // 试件ID
    private String specimenType;       // 试件类型
    private String materialType;       // 材料类型
    private String remarks;            // 备注
    
    // 存储所有实验参数的键值对
    private Map<String, String> experimentValues;
}
