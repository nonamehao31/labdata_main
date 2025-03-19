package com.example.labdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * 支持的混合料任务类型实体类
 */
@Entity
@Table(name = "support_mixture_task")
public class SupportMixtureTask implements Serializable {
    
    @Id
    @Column(name = "task_id")
    private Integer taskId;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "task_type")
    private String taskType;
    
    // 默认构造函数
    public SupportMixtureTask() {}
    
    // 带参数的构造函数
    public SupportMixtureTask(Integer taskId, String taskName, String taskType) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
    }
    
    // Getters and Setters
    public Integer getTaskId() {
        return taskId;
    }
    
    public void setTaskId(Integer taskId) {
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
    
    @Override
    public String toString() {
        return "SupportMixtureTask{" +
                "taskId=" + taskId +
                ", taskName='" + taskName + '\'' +
                ", taskType='" + taskType + '\'' +
                '}';
    }
}
