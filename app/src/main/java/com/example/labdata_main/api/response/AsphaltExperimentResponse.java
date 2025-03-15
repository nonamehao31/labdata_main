package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青实验响应体
 */
public class AsphaltExperimentResponse {
    @SerializedName("asphalt_experiment_id")
    private Long asphaltExperimentId;
    
    @SerializedName("asphalt_experiment_name")
    private String asphaltExperimentName;
    
    @SerializedName("asphalt_experiment_type")
    private String asphaltExperimentType;

    public Long getAsphaltExperimentId() {
        return asphaltExperimentId;
    }

    public void setAsphaltExperimentId(Long asphaltExperimentId) {
        this.asphaltExperimentId = asphaltExperimentId;
    }

    public String getAsphaltExperimentName() {
        return asphaltExperimentName;
    }

    public void setAsphaltExperimentName(String asphaltExperimentName) {
        this.asphaltExperimentName = asphaltExperimentName;
    }

    public String getAsphaltExperimentType() {
        return asphaltExperimentType;
    }

    public void setAsphaltExperimentType(String asphaltExperimentType) {
        this.asphaltExperimentType = asphaltExperimentType;
    }
}
