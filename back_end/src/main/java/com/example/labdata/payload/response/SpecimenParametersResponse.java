package com.example.labdata.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 制件参数响应类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecimenParametersResponse {
    private Long id;                 // 制件方法ID
    private Float mixingTemperature; // 拌合温度
    private Float mixingSpeed;       // 拌合速度
    private Integer mixingTime;      // 拌合时间
    private String compactionMethod; // 压实方法
}
