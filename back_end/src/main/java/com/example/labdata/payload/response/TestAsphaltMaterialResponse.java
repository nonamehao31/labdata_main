package com.example.labdata.payload.response;

import com.example.labdata.model.TestAsphaltMaterial;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 沥青材料响应DTO
 */
@Data
@NoArgsConstructor
public class TestAsphaltMaterialResponse {
    
    private Long id;
    
    private String asphaltSupplier;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate asphaltTestDue;
    
    private String asphaltGrade;
    
    private String asphaltCatalog;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private Long organizationId;
    
    private Long createdBy;
    
    /**
     * 将实体转换为响应DTO
     * @param asphaltMaterial 沥青材料实体
     * @return 响应DTO
     */
    public static TestAsphaltMaterialResponse fromEntity(TestAsphaltMaterial asphaltMaterial) {
        TestAsphaltMaterialResponse response = new TestAsphaltMaterialResponse();
        response.setId(asphaltMaterial.getId());
        response.setAsphaltSupplier(asphaltMaterial.getAsphaltSupplier());
        response.setAsphaltTestDue(asphaltMaterial.getAsphaltTestDue());
        response.setAsphaltGrade(asphaltMaterial.getAsphaltGrade());
        response.setAsphaltCatalog(asphaltMaterial.getAsphaltCatalog());
        response.setCreatedAt(asphaltMaterial.getCreatedAt());
        response.setUpdatedAt(asphaltMaterial.getUpdatedAt());
        response.setOrganizationId(asphaltMaterial.getOrganizationId());
        response.setCreatedBy(asphaltMaterial.getCreatedBy());
        return response;
    }
    
    /**
     * 将实体列表转换为响应DTO列表
     * @param asphaltMaterials 沥青材料实体列表
     * @return 响应DTO列表
     */
    public static List<TestAsphaltMaterialResponse> fromEntities(List<TestAsphaltMaterial> asphaltMaterials) {
        return asphaltMaterials.stream()
                .map(TestAsphaltMaterialResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
