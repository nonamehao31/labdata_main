package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 动态模量实验数据响应模型
 */
public class DynamicModulusTestResponse {
    
    @SerializedName("test_id")
    private long testId;
    
    @SerializedName("task_id")
    private String taskId;
    
    @SerializedName("mix_ratio_name")
    private String mixRatioName;
    
    @SerializedName("specimen")
    private Specimen specimen;
    
    @SerializedName("measurements")
    private List<TemperatureGroup> temperatureGroups;
    
    // 试件信息
    public static class Specimen {
        @SerializedName("specimen_number")
        private int specimenNumber;
        
        @SerializedName("diameter")
        private float diameter;
        
        @SerializedName("height")
        private float height;
        
        @SerializedName("bulk_density")
        private float bulkDensity;
        
        @SerializedName("air_void_content")
        private float airVoidContent;
        
        // Getters and Setters
        public int getSpecimenNumber() {
            return specimenNumber;
        }
        
        public void setSpecimenNumber(int specimenNumber) {
            this.specimenNumber = specimenNumber;
        }
        
        public float getDiameter() {
            return diameter;
        }
        
        public void setDiameter(float diameter) {
            this.diameter = diameter;
        }
        
        public float getHeight() {
            return height;
        }
        
        public void setHeight(float height) {
            this.height = height;
        }
        
        public float getBulkDensity() {
            return bulkDensity;
        }
        
        public void setBulkDensity(float bulkDensity) {
            this.bulkDensity = bulkDensity;
        }
        
        public float getAirVoidContent() {
            return airVoidContent;
        }
        
        public void setAirVoidContent(float airVoidContent) {
            this.airVoidContent = airVoidContent;
        }
    }
    
    // 温度分组
    public static class TemperatureGroup {
        @SerializedName("temperature")
        private float temperature;
        
        @SerializedName("measurements")
        private List<Measurement> measurements;
        
        // Getters and Setters
        public float getTemperature() {
            return temperature;
        }
        
        public void setTemperature(float temperature) {
            this.temperature = temperature;
        }
        
        public List<Measurement> getMeasurements() {
            return measurements;
        }
        
        public void setMeasurements(List<Measurement> measurements) {
            this.measurements = measurements;
        }
    }
    
    // 测量数据
    public static class Measurement {
        @SerializedName("frequency")
        private float frequency;
        
        @SerializedName("cycle_count")
        private int cycleCount;
        
        @SerializedName("dynamic_modulus")
        private float dynamicModulus;
        
        @SerializedName("phase_angle")
        private float phaseAngle;
        
        // Getters and Setters
        public float getFrequency() {
            return frequency;
        }
        
        public void setFrequency(float frequency) {
            this.frequency = frequency;
        }
        
        public int getCycleCount() {
            return cycleCount;
        }
        
        public void setCycleCount(int cycleCount) {
            this.cycleCount = cycleCount;
        }
        
        public float getDynamicModulus() {
            return dynamicModulus;
        }
        
        public void setDynamicModulus(float dynamicModulus) {
            this.dynamicModulus = dynamicModulus;
        }
        
        public float getPhaseAngle() {
            return phaseAngle;
        }
        
        public void setPhaseAngle(float phaseAngle) {
            this.phaseAngle = phaseAngle;
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
    
    public String getMixRatioName() {
        return mixRatioName;
    }
    
    public void setMixRatioName(String mixRatioName) {
        this.mixRatioName = mixRatioName;
    }
    
    public Specimen getSpecimen() {
        return specimen;
    }
    
    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }
    
    public List<TemperatureGroup> getTemperatureGroups() {
        return temperatureGroups;
    }
    
    public void setTemperatureGroups(List<TemperatureGroup> temperatureGroups) {
        this.temperatureGroups = temperatureGroups;
    }
}
