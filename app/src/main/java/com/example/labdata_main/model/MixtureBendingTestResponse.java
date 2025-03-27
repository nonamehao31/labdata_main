package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青混合料弯曲试验数据响应模型
 */
public class MixtureBendingTestResponse {
    private Long id;
    
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("mix_ratio_id")
    private Long mixRatioId;
    
    @SerializedName("span_length")
    private Float spanLength;
    
    @SerializedName("specimen_count")
    private Integer specimenCount;
    
    @SerializedName("average_flexural_strength")
    private Float averageFlexuralStrength;
    
    @SerializedName("average_max_strain")
    private Float averageMaxStrain;
    
    @SerializedName("average_stiffness_modulus")
    private Float averageStiffnessModulus;
    
    @SerializedName("specimens")
    private String specimens;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(Long mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public Float getSpanLength() {
        return spanLength;
    }

    public void setSpanLength(Float spanLength) {
        this.spanLength = spanLength;
    }

    public Integer getSpecimenCount() {
        return specimenCount;
    }

    public void setSpecimenCount(Integer specimenCount) {
        this.specimenCount = specimenCount;
    }

    public Float getAverageFlexuralStrength() {
        return averageFlexuralStrength;
    }

    public void setAverageFlexuralStrength(Float averageFlexuralStrength) {
        this.averageFlexuralStrength = averageFlexuralStrength;
    }

    public Float getAverageMaxStrain() {
        return averageMaxStrain;
    }

    public void setAverageMaxStrain(Float averageMaxStrain) {
        this.averageMaxStrain = averageMaxStrain;
    }

    public Float getAverageStiffnessModulus() {
        return averageStiffnessModulus;
    }

    public void setAverageStiffnessModulus(Float averageStiffnessModulus) {
        this.averageStiffnessModulus = averageStiffnessModulus;
    }

    public String getSpecimens() {
        return specimens;
    }

    public void setSpecimens(String specimens) {
        this.specimens = specimens;
    }
}
