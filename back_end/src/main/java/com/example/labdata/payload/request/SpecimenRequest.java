package com.example.labdata.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecimenRequest {
    
    // 移除@NotNull注解，允许mixRatioId为空
    private Long mixRatioId;
    
    @NotNull(message = "拌合温度不能为空")
    @PositiveOrZero(message = "拌合温度必须大于或等于0")
    private Float mixingTemperature;
    
    @NotNull(message = "拌合速度不能为空")
    @PositiveOrZero(message = "拌合速度必须大于或等于0")
    private Float mixingSpeed;
    
    @PositiveOrZero(message = "拌合时间必须大于或等于0")
    private Integer mixingTime;
    
    private String compactionMethod;
    
    // 添加创建者ID字段，后端会自动根据当前登录用户设置
    private Long createdBy;
    
    // 添加所属单位ID字段
    private Long specimenCompany;
    
    // 添加创建时间字段
    private Long creationTime;
    
    private String cutShape;
    
    private Integer cutCount;
    
    private Float length;
    
    private Float width;
    
    private Float height;
    
    private Float radius;
}
