package com.example.labdata.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 沥青材料请求DTO
 */
@Data
public class TestAsphaltMaterialRequest {
    
    /**
     * 沥青供应商
     */
    @NotBlank(message = "沥青供应商不能为空")
    private String asphaltSupplier;
    
    /**
     * 检测截止日期
     */
    @NotNull(message = "检测截止日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate asphaltTestDue;
    
    /**
     * 沥青标号
     */
    @NotBlank(message = "沥青标号不能为空")
    private String asphaltGrade;
    
    /**
     * 沥青类型（NORMAL和MODIFIED）
     */
    @NotBlank(message = "沥青类型不能为空")
    private String asphaltCatalog;
    
    /**
     * 所属项目ID（可选）
     */
    private Long projectId;
    
    /**
     * 备注信息（可选）
     */
    private String remarks;
}
