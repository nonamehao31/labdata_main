package com.example.labmanagement.model;

// DEPRECATED: 此类已迁移到 com.example.labdata_main.model.ExperimentTask
// 请使用新的包路径，此类将在未来版本中移除

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
