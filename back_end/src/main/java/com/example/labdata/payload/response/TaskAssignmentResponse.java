package com.example.labdata.payload.response;

/**
 * 任务指派信息响应模型
 */
public class TaskAssignmentResponse {
    private String taskAssignment;
    private int code;
    private String message;
    
    // 设备指派信息
    private String assignedMixingEquipment;
    private String mixingEquipmentManufacturer;
    private String assignedFormingEquipment;
    private String formingEquipmentManufacturer;
    private String assignedTestingEquipment;
    private String testingEquipmentManufacturer;

    public TaskAssignmentResponse() {
    }

    public TaskAssignmentResponse(String taskAssignment) {
        this.taskAssignment = taskAssignment;
        this.code = 200;
        this.message = "success";
    }
    
    public TaskAssignmentResponse(String taskAssignment, String assignedMixingEquipment, String mixingEquipmentManufacturer,
                                 String assignedFormingEquipment, String formingEquipmentManufacturer,
                                 String assignedTestingEquipment, String testingEquipmentManufacturer) {
        this.taskAssignment = taskAssignment;
        this.assignedMixingEquipment = assignedMixingEquipment;
        this.mixingEquipmentManufacturer = mixingEquipmentManufacturer;
        this.assignedFormingEquipment = assignedFormingEquipment;
        this.formingEquipmentManufacturer = formingEquipmentManufacturer;
        this.assignedTestingEquipment = assignedTestingEquipment;
        this.testingEquipmentManufacturer = testingEquipmentManufacturer;
        this.code = 200;
        this.message = "success";
    }

    public TaskAssignmentResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getTaskAssignment() {
        return taskAssignment;
    }

    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAssignedMixingEquipment() {
        return assignedMixingEquipment;
    }

    public void setAssignedMixingEquipment(String assignedMixingEquipment) {
        this.assignedMixingEquipment = assignedMixingEquipment;
    }

    public String getMixingEquipmentManufacturer() {
        return mixingEquipmentManufacturer;
    }

    public void setMixingEquipmentManufacturer(String mixingEquipmentManufacturer) {
        this.mixingEquipmentManufacturer = mixingEquipmentManufacturer;
    }

    public String getAssignedFormingEquipment() {
        return assignedFormingEquipment;
    }

    public void setAssignedFormingEquipment(String assignedFormingEquipment) {
        this.assignedFormingEquipment = assignedFormingEquipment;
    }

    public String getFormingEquipmentManufacturer() {
        return formingEquipmentManufacturer;
    }

    public void setFormingEquipmentManufacturer(String formingEquipmentManufacturer) {
        this.formingEquipmentManufacturer = formingEquipmentManufacturer;
    }

    public String getAssignedTestingEquipment() {
        return assignedTestingEquipment;
    }

    public void setAssignedTestingEquipment(String assignedTestingEquipment) {
        this.assignedTestingEquipment = assignedTestingEquipment;
    }

    public String getTestingEquipmentManufacturer() {
        return testingEquipmentManufacturer;
    }

    public void setTestingEquipmentManufacturer(String testingEquipmentManufacturer) {
        this.testingEquipmentManufacturer = testingEquipmentManufacturer;
    }
}
