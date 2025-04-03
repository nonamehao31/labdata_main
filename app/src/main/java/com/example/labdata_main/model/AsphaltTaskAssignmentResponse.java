package com.example.labdata_main.model;

import java.io.Serializable;

/**
 * 沥青实验任务指派信息响应模型
 */
public class AsphaltTaskAssignmentResponse implements Serializable {
    private String taskId;
    private String taskAssignment;
    private String assignedAsphaltEquipment;
    private String asphaltEquipmentManufacturer;

    public AsphaltTaskAssignmentResponse() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskAssignment() {
        return taskAssignment;
    }

    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }

    public String getAssignedAsphaltEquipment() {
        return assignedAsphaltEquipment;
    }

    public void setAssignedAsphaltEquipment(String assignedAsphaltEquipment) {
        this.assignedAsphaltEquipment = assignedAsphaltEquipment;
    }

    public String getAsphaltEquipmentManufacturer() {
        return asphaltEquipmentManufacturer;
    }

    public void setAsphaltEquipmentManufacturer(String asphaltEquipmentManufacturer) {
        this.asphaltEquipmentManufacturer = asphaltEquipmentManufacturer;
    }
}
