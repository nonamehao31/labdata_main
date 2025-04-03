package com.example.labdata.payload.response;

/**
 * 沥青实验任务指派信息响应类
 */
public class AsphaltTaskAssignmentResponse {
    private String taskId;
    private String taskAssignment;
    private String assignedAsphaltEquipment;
    private String asphaltEquipmentManufacturer;

    public AsphaltTaskAssignmentResponse() {
    }

    public AsphaltTaskAssignmentResponse(String taskId, String taskAssignment, 
                                        String assignedAsphaltEquipment, 
                                        String asphaltEquipmentManufacturer) {
        this.taskId = taskId;
        this.taskAssignment = taskAssignment;
        this.assignedAsphaltEquipment = assignedAsphaltEquipment;
        this.asphaltEquipmentManufacturer = asphaltEquipmentManufacturer;
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
