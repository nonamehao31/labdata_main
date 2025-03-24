package com.example.labdata.payload.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 布鲁克菲尔德旋转黏度实验请求类
 */
@Data
public class BrookfieldViscosityTestRequest {

    private String taskId;                    // 任务ID
    private String experimenter;              // 实验人员
    private Long testDate;                    // 测试日期
    private String deviceId;                  // 设备ID
    private String deviceName;                // 设备名称
    private String deviceManufacturer;        // 设备制造商
    private String deviceModel;               // 设备型号
    private Map<String, String> experimentValues; // 实验值映射
}
