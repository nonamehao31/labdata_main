package com.example.labdata.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class MixRatioRequest {
    @NotBlank(message = "配合比名称不能为空")
    private String mixName;
    
    @NotEmpty(message = "沥青材料不能为空")
    @Valid
    private List<AsphaltComponentRequest> asphaltComponents;
    
    @NotEmpty(message = "沙子材料不能为空")
    @Valid
    private List<SandComponentRequest> sandComponents;
    
    @NotEmpty(message = "石子材料不能为空")
    @Valid
    private List<StoneComponentRequest> stoneComponents;
    
    // 添加用户单位ID和用户ID字段
    private Long mixCompany;
    private Long createdBy;
    
    @Data
    public static class AsphaltComponentRequest {
        private Long asphaltId;
        private Double percentage;
    }
    
    @Data
    public static class SandComponentRequest {
        private Long sandId;
        private String gradation;
        private Double percentage;
    }
    
    @Data
    public static class StoneComponentRequest {
        private Long stoneId;
        private String gradation;
        private Double percentage;
    }
}
