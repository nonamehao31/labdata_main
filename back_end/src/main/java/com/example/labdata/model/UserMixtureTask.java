package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户混合料任务实体类
 * 对应mixture_task表
 */
@Entity
@Table(name = "mixture_task")
@Data
@NoArgsConstructor
public class UserMixtureTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private String taskId;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "task_company", nullable = false)
    private Long taskCompany;
    
    @Column(name = "est_by", nullable = false)
    private Long estBy;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "mixratio_id", nullable = false)
    private Long mixratioId;
    
    @Column(name = "specimen_id", nullable = false)
    private Long specimenId;
    
    @Column(name = "task_assignment")
    private String taskAssignment;
    
    @Column(name = "remarks")
    private String remarks;
    
    @Column(name = "creation_time", nullable = false)
    private Long creationTime;
    
    @Column(name = "status", columnDefinition = "varchar(20) default 'CREATED'")
    private String status = "CREATED";
    
    /**
     * 根据当前任务自动设置任务ID，格式为 MIXTURE_yyyyMMdd_序号
     */
    public void generateTaskId() {
        // 已在数据库中实现
    }

    /**
     * 获取默认任务名称
     * 当任务名称为空时使用
     * @return 默认任务名称
     */
    public String getDefaultTaskName() {
        if (taskName == null || taskName.isEmpty()) {
            return "混合料任务-" + (id != null ? id : "未知");
        }
        return taskName;
    }

    /**
     * 创建新任务实例的便捷方法
     */
    public static UserMixtureTask createTask(String taskId, Long taskCompany, Long estBy, 
                                           Long projectId, Long mixratioId, Long specimenId,
                                           String taskAssignment, String remarks, String taskName) {
        UserMixtureTask task = new UserMixtureTask();
        task.setTaskId(taskId);
        task.setTaskCompany(taskCompany);
        task.setEstBy(estBy);
        task.setProjectId(projectId);
        task.setMixratioId(mixratioId);
        task.setSpecimenId(specimenId);
        task.setTaskAssignment(taskAssignment);
        task.setRemarks(remarks);
        task.setTaskName(taskName);
        task.setCreationTime(System.currentTimeMillis());
        task.setStatus("CREATED");
        return task;
    }
    
    /**
     * 向后兼容的创建方法 (不含任务名称)
     */
    public static UserMixtureTask createTask(String taskId, Long taskCompany, Long estBy, 
                                           Long projectId, Long mixratioId, Long specimenId,
                                           String taskAssignment, String remarks) {
        return createTask(taskId, taskCompany, estBy, projectId, mixratioId, specimenId, 
                        taskAssignment, remarks, null);
    }
}
