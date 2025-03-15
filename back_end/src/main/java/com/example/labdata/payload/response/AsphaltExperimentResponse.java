package com.example.labdata.payload.response;

import com.example.labdata.model.AsphaltTask;

/**
 * 沥青实验响应体
 */
public class AsphaltExperimentResponse {
    private Long asphaltExperimentId;
    private String asphaltExperimentName;
    private String asphaltExperimentType;

    public AsphaltExperimentResponse() {
    }

    public AsphaltExperimentResponse(AsphaltTask asphaltTask) {
        this.asphaltExperimentId = asphaltTask.getAsphaltExperimentId();
        this.asphaltExperimentName = asphaltTask.getAsphaltExperimentName();
        this.asphaltExperimentType = asphaltTask.getAsphaltExperimentType();
    }

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
