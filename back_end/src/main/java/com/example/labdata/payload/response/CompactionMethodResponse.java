package com.example.labdata.payload.response;

import com.example.labdata.model.CompactionMethod;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 制件方法响应DTO
 * 将后端实体映射为适合前端使用的数据格式
 */
@Data
@NoArgsConstructor
public class CompactionMethodResponse {
    
    private Long id;
    
    // 适配前端MoldingMethod的类型字段
    private Integer type;
    
    // 适配前端MoldingMethod的字段
    private Float size;
    private Float diameter;
    private Float length;
    private Float width;
    private Float height;
    private Integer count;
    
    // 拌合相关字段
    private Float mixingTemperature;
    private Float mixingSpeed;
    private Float mixingTime;
    
    // 压实方法
    private String compactionMethod;
    
    // 其他必要信息
    private Long mixRatioId;
    private Integer methodIndex;
    private String methodGroup;
    
    // 后端特有字段
    private String organization;
    private String methodName;
    
    /**
     * 从CompactionMethod实体创建响应DTO
     * @param compactionMethod 制件方法实体
     * @return 响应DTO
     */
    public static CompactionMethodResponse fromEntity(CompactionMethod compactionMethod) {
        if (compactionMethod == null) {
            return null;
        }
        
        CompactionMethodResponse response = new CompactionMethodResponse();
        response.setId(compactionMethod.getId());
        response.setType(compactionMethod.getSpecimenType());
        
        // 设置拌合相关字段
        response.setMixingTemperature(compactionMethod.getMixingTemperature());
        response.setMixingSpeed(compactionMethod.getMixingSpeed());
        response.setMixingTime(compactionMethod.getMixingTime());
        
        // 设置压实方法
        response.setCompactionMethod(compactionMethod.getCompactionMethod());
        
        // 设置其他必要信息
        response.setOrganization(compactionMethod.getOrganization());
        response.setMethodName(compactionMethod.getMethodName());
        
        return response;
    }
}
