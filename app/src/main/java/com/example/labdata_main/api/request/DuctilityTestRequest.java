package com.example.labdata_main.api.request;

/**
 * 延度实验数据请求类
 */
public class DuctilityTestRequest {
    private String taskId;
    private String temperature; // 温度
    private String displacement; // 拉长位移
    private String experimenter; // 实验人员
    private long testDate; // 试验日期
    private String deviceId; // 设备ID
    private String deviceName; // 设备名称
    private String deviceManufacturer; // 设备制造商
    private String deviceModel; // 设备型号

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getDisplacement() {
        return displacement;
    }

    public void setDisplacement(String displacement) {
        this.displacement = displacement;
    }

    public String getExperimenter() {
        return experimenter;
    }

    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
    }

    public long getTestDate() {
        return testDate;
    }

    public void setTestDate(long testDate) {
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

    @Override
    public String toString() {
        return "DuctilityTestRequest{" +
                "taskId='" + taskId + '\'' +
                ", temperature='" + temperature + '\'' +
                ", displacement='" + displacement + '\'' +
                ", experimenter='" + experimenter + '\'' +
                ", testDate=" + testDate +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceManufacturer='" + deviceManufacturer + '\'' +
                ", deviceModel='" + deviceModel + '\'' +
                '}';
    }
}
