package com.example.labdata.payload.response;

import com.example.labdata.model.MixtureBendingTest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 沥青混合料弯曲试验数据传输对象
 */
@Data
public class MixtureBendingTestDTO {

    private Long id;
    
    @JsonProperty("task_id")
    private String taskId;
    
    @JsonProperty("mix_ratio_id")
    private Long mixRatioId;
    
    @JsonProperty("span_length")
    private Float spanLength;
    
    @JsonProperty("specimen_count")
    private Integer specimenCount;
    
    @JsonProperty("average_flexural_strength")
    private Float averageFlexuralStrength;
    
    @JsonProperty("average_max_strain")
    private Float averageMaxStrain;
    
    @JsonProperty("average_stiffness_modulus")
    private Float averageStiffnessModulus;
    
    @JsonProperty("specimens")
    private String specimens;
    
    /**
     * 将实体对象转换为DTO对象
     * @param entity 实体对象
     * @return DTO对象
     */
    public static MixtureBendingTestDTO fromEntity(MixtureBendingTest entity) {
        MixtureBendingTestDTO dto = new MixtureBendingTestDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setMixRatioId(entity.getMixRatioId());
        dto.setSpanLength(entity.getSpanLength());
        dto.setSpecimenCount(entity.getSpecimenCount());
        dto.setAverageFlexuralStrength(entity.getAverageFlexuralStrength());
        dto.setAverageMaxStrain(entity.getAverageMaxStrain());
        dto.setAverageStiffnessModulus(entity.getAverageStiffnessModulus());
        dto.setSpecimens(entity.getSpecimens());
        return dto;
    }
}
