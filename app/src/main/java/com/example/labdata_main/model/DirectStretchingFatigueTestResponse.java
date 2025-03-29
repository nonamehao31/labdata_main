package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据响应模型
 */
public class DirectStretchingFatigueTestResponse {
    
    @SerializedName("test_id")
    private long testId;
    
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("mix_ratio_id")
    private String mixRatioId;
    
    @SerializedName("mix_ratio_name")
    private String mixRatioName;
    
    @SerializedName("specimens")
    private List<Specimen> specimens;
    
    // 试件信息
    public static class Specimen {
        @SerializedName("specimen_id")
        private String specimenId;
        
        @SerializedName("height")
        private float height;
        
        @SerializedName("diameter")
        private float diameter;
        
        @SerializedName("modulus_data")
        private List<ModulusData> modulusData;
        
        @SerializedName("fatigue_data")
        private List<FatigueData> fatigueData;
        
        // Getters and Setters
        public String getSpecimenId() {
            return specimenId;
        }
        
        public void setSpecimenId(String specimenId) {
            this.specimenId = specimenId;
        }
        
        public float getHeight() {
            return height;
        }
        
        public void setHeight(float height) {
            this.height = height;
        }
        
        public float getDiameter() {
            return diameter;
        }
        
        public void setDiameter(float diameter) {
            this.diameter = diameter;
        }
        
        public List<ModulusData> getModulusData() {
            return modulusData;
        }
        
        public void setModulusData(List<ModulusData> modulusData) {
            this.modulusData = modulusData;
        }
        
        public List<FatigueData> getFatigueData() {
            return fatigueData;
        }
        
        public void setFatigueData(List<FatigueData> fatigueData) {
            this.fatigueData = fatigueData;
        }
    }
    
    // 动态模量数据
    public static class ModulusData {
        @SerializedName("stage")
        private String stage;
        
        @SerializedName("dynamic_modulus")
        private float dynamicModulus;
        
        @SerializedName("cycle_count")
        private int cycleCount;
        
        @SerializedName("phase_angle")
        private float phaseAngle;
        
        @SerializedName("force_level")
        private float forceLevel;
        
        @SerializedName("equilibrium_strain")
        private float equilibriumStrain;
        
        @SerializedName("temperature")
        private float temperature;
        
        // Getters and Setters
        public String getStage() {
            return stage;
        }
        
        public void setStage(String stage) {
            this.stage = stage;
        }
        
        public float getDynamicModulus() {
            return dynamicModulus > 0.001f ? dynamicModulus : 0;
        }
        
        public void setDynamicModulus(float dynamicModulus) {
            this.dynamicModulus = dynamicModulus;
        }
        
        public int getCycleCount() {
            return cycleCount > 0 ? cycleCount : 0;
        }
        
        public void setCycleCount(int cycleCount) {
            this.cycleCount = cycleCount;
        }
        
        public float getPhaseAngle() {
            return phaseAngle > 0.001f ? phaseAngle : 0;
        }
        
        public void setPhaseAngle(float phaseAngle) {
            this.phaseAngle = phaseAngle;
        }
        
        public float getForceLevel() {
            return forceLevel > 0.001f ? forceLevel : 0;
        }
        
        public void setForceLevel(float forceLevel) {
            this.forceLevel = forceLevel;
        }
        
        public float getEquilibriumStrain() {
            return equilibriumStrain > 0.001f ? equilibriumStrain : 0;
        }
        
        public void setEquilibriumStrain(float equilibriumStrain) {
            this.equilibriumStrain = equilibriumStrain;
        }
        
        public float getTemperature() {
            return temperature;
        }
        
        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }
    }
    
    // 疲劳数据
    public static class FatigueData {
        @SerializedName("stage")
        private String stage;
        
        @SerializedName("dynamic_modulus")
        private float dynamicModulus;
        
        @SerializedName("cycle_count")
        private int cycleCount;
        
        @SerializedName("phase_angle")
        private float phaseAngle;
        
        @SerializedName("force_level")
        private float forceLevel;
        
        @SerializedName("equilibrium_strain")
        private float equilibriumStrain;
        
        @SerializedName("temperature")
        private float temperature;
        
        // Getters and Setters
        public String getStage() {
            return stage;
        }
        
        public void setStage(String stage) {
            this.stage = stage;
        }
        
        public float getDynamicModulus() {
            return dynamicModulus > 0.001f ? dynamicModulus : 0;
        }
        
        public void setDynamicModulus(float dynamicModulus) {
            this.dynamicModulus = dynamicModulus;
        }
        
        public int getCycleCount() {
            return cycleCount > 0 ? cycleCount : 0;
        }
        
        public void setCycleCount(int cycleCount) {
            this.cycleCount = cycleCount;
        }
        
        public float getPhaseAngle() {
            return phaseAngle > 0.001f ? phaseAngle : 0;
        }
        
        public void setPhaseAngle(float phaseAngle) {
            this.phaseAngle = phaseAngle;
        }
        
        public float getForceLevel() {
            return forceLevel > 0.001f ? forceLevel : 0;
        }
        
        public void setForceLevel(float forceLevel) {
            this.forceLevel = forceLevel;
        }
        
        public float getEquilibriumStrain() {
            return equilibriumStrain > 0.001f ? equilibriumStrain : 0;
        }
        
        public void setEquilibriumStrain(float equilibriumStrain) {
            this.equilibriumStrain = equilibriumStrain;
        }
        
        public float getTemperature() {
            return temperature;
        }
        
        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }
    }
    
    // Getters and Setters
    public long getTestId() {
        return testId;
    }
    
    public void setTestId(long testId) {
        this.testId = testId;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getMixRatioId() {
        return mixRatioId;
    }
    
    public void setMixRatioId(String mixRatioId) {
        this.mixRatioId = mixRatioId;
    }
    
    public String getMixRatioName() {
        return mixRatioName;
    }
    
    public void setMixRatioName(String mixRatioName) {
        this.mixRatioName = mixRatioName;
    }
    
    public List<Specimen> getSpecimens() {
        return specimens;
    }
    
    public void setSpecimens(List<Specimen> specimens) {
        this.specimens = specimens;
    }
}
