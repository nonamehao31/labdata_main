package com.example.labdata_main.model;

public class ExperimentTask {
    private String taskName;
    private String taskInfo;
    private boolean isCompleted;

    public ExperimentTask(String taskName, String taskInfo, boolean isCompleted) {
        this.taskName = taskName;
        this.taskInfo = taskInfo;
        this.isCompleted = isCompleted;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskInfo() {
        return taskInfo;
    }

    public void setTaskInfo(String taskInfo) {
        this.taskInfo = taskInfo;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
