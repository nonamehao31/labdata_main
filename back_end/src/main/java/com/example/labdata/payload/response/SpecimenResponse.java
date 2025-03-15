package com.example.labdata.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecimenResponse {
    
    private Long id;
    private Long mixRatioId;
    private Float mixingTemperature;
    private Float mixingSpeed;
    private Integer mixingTime;
    private String compactionMethod;
    private Long creationTime;
    private String cutShape;
    private Integer cutCount;
    private Float length;
    private Float width;
    private Float height;
    private Float radius;
    private String mixRatioName; // 配比名称，用于显示
}
