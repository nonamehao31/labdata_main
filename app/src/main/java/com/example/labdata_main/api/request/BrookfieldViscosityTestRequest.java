package com.example.labdata_main.api.request;

import java.util.Map;

public class BrookfieldViscosityTestRequest {
    private String taskId;
    private String experimenter;
    private Long testDate;
    private String deviceId;
    private String deviceName;
    private String deviceManufacturer;
    private String deviceModel;
    private Map<String, String> experimentValues;

    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getExperimenter() {
        return experimenter;
    }

    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
    }

    public Long getTestDate() {
        return testDate;
    }

    public void setTestDate(Long testDate) {
        this.testDate = testDate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public void setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public Map<String, String> getExperimentValues() {
        return experimentValues;
    }

    public void setExperimentValues(Map<String, String> experimentValues) {
        this.experimentValues = experimentValues;
    }

    @Override
    public String toString() {
        return "BrookfieldViscosityTestRequest{" +
                "taskId='" + taskId + '\'' +
                ", experimenter='" + experimenter + '\'' +
                ", testDate=" + testDate +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                ", experimentValues=" + experimentValues +
                '}';
    }
}