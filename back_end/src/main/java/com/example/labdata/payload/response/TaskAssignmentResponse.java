package com.example.labdata.payload.response;

/**
 * 任务指派信息响应模型
 */
public class TaskAssignmentResponse {
    private String taskAssignment;
    private int code;
    private String message;

    public TaskAssignmentResponse() {
    }

    public TaskAssignmentResponse(String taskAssignment) {
        this.taskAssignment = taskAssignment;
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
}
