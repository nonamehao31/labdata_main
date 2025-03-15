package com.example.labdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "support_mixture_task")
public class MixtureTask {
    @Id
    @Column(name = "task_id")
    private Long taskId;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "task_type")
    private String taskType;
    
    // Constructors
    public MixtureTask() {}

    public MixtureTask(Long taskId, String taskName, String taskType) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
    }
    
    // Getters and Setters
    public Long getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
}
