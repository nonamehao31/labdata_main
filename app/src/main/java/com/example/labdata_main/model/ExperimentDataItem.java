package com.example.labdata_main.model;

public class ExperimentDataItem {
    private String experimentName;
    private String input1Label;
    private String input2Label;
    private double input1Value;
    private double input2Value;
    private DeviceInfo deviceInfo;

    public ExperimentDataItem(String experimentName, String input1Label, String input2Label) {
        this.experimentName = experimentName;
        this.input1Label = input1Label;
        this.input2Label = input2Label;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getInput1Label() {
        return input1Label;
    }

    public String getInput2Label() {
        return input2Label;
    }

    public double getInput1Value() {
        return input1Value;
    }

    public void setInput1Value(double input1Value) {
        this.input1Value = input1Value;
    }

    public double getInput2Value() {
        return input2Value;
    }

    public void setInput2Value(double input2Value) {
        this.input2Value = input2Value;
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public boolean hasSecondInput() {
        return input2Label != null && !input2Label.isEmpty();
    }

    public boolean hasDevice() {
        return deviceInfo != null;
    }
}
