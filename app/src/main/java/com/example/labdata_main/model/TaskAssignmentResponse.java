package com.example.labdata_main.model;

/**
 * 实验任务指派信息响应模型
 */
public class TaskAssignmentResponse {
    private String taskAssignment;
    private int code;
    private String message;

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
}
