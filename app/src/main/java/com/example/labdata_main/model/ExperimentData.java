package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "experiment_data",
        foreignKeys = @ForeignKey(
                entity = ExperimentTask.class,
                parentColumns = "id",
                childColumns = "taskId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index(value = {"experimentName"}),
                @Index(value = {"experimentName", "input1Label", "input1Value"}),
                @Index(value = {"experimentName", "input2Label", "input2Value"}),
                @Index(value = {"deviceId", "deviceManufacturer", "deviceModel"}),
                @Index(value = {"taskId"}),
                @Index(value = {"mixRatio"}),
                @Index(value = {"result"})
        })
public class ExperimentData {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long taskId;
    private String experimentName;
    private String input1Label;
    private String input2Label;
    private double input1Value;
    private double input2Value;
    
    // 设备信息
    private String deviceId;
    private String deviceManufacturer;
    private String deviceModel;
    private String devicePurchaseYear;
    private String mixRatio;  // 添加配比字段
    private String result;    // 添加结果字段

    private long createTime;

    public ExperimentData() {
        this.createTime = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }

    public String getInput1Label() {
        return input1Label;
    }

    public void setInput1Label(String input1Label) {
        this.input1Label = input1Label;
    }

    public String getInput2Label() {
        return input2Label;
    }

    public void setInput2Label(String input2Label) {
        this.input2Label = input2Label;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getDevicePurchaseYear() {
        return devicePurchaseYear;
    }

    public void setDevicePurchaseYear(String devicePurchaseYear) {
        this.devicePurchaseYear = devicePurchaseYear;
    }

    public String getMixRatio() {
        return mixRatio;
    }

    public void setMixRatio(String mixRatio) {
        this.mixRatio = mixRatio;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getResultString() {
        StringBuilder result = new StringBuilder();
        if (input1Label != null && input1Value != Double.NaN) {
            result.append(input1Label).append("=").append(input1Value);
        }
        if (input2Label != null && !input2Label.isEmpty() && input2Value != Double.NaN) {
            result.append(", ").append(input2Label).append("=").append(input2Value);
        }
        return result.toString();
    }
}
