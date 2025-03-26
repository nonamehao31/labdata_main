package com.example.labdata.payload.dto;

/**
 * DSR实验数据点DTO
 */
public class DsrDataPoint {
    private Double temperature;
    private Double frequency;
    private Double maxShearStress;
    private Double maxShearStrain;
    private Double phaseAngle;
    private Double complexModulus;  // 复合剪切模量G*

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public Double getMaxShearStress() {
        return maxShearStress;
    }

    public void setMaxShearStress(Double maxShearStress) {
        this.maxShearStress = maxShearStress;
    }

    public Double getMaxShearStrain() {
        return maxShearStrain;
    }

    public void setMaxShearStrain(Double maxShearStrain) {
        this.maxShearStrain = maxShearStrain;
    }

    public Double getPhaseAngle() {
        return phaseAngle;
    }

    public void setPhaseAngle(Double phaseAngle) {
        this.phaseAngle = phaseAngle;
    }

    public Double getComplexModulus() {
        return complexModulus;
    }

    public void setComplexModulus(Double complexModulus) {
        this.complexModulus = complexModulus;
    }
}
