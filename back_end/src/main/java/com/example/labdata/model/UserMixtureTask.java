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
    private String taskCompany;
    
    @Column(name = "est_by", nullable = false)
    private Long estBy;
    
    /**
     * 这个字段实际上与Project表中的project_id字段相关联，而不是Project表的id
     */
    @Column(name = "project_id", nullable = false)
    private String projectId;
    
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

    @Column(name = "due_date")
    private String dueDate;
    
    @Column(name = "acceptor")
    private String acceptor;
    
    @Column(name = "accept_time")
    private Long acceptTime;
    
    @Column(name = "prepare_status", columnDefinition = "varchar(20) default 'unfinished'")
    private String prepareStatus = "unfinished";
    
    @Column(name = "making_status", columnDefinition = "varchar(20) default 'unfinished'")
    private String makingStatus = "unfinished";
    
    @Column(name = "testing_status", columnDefinition = "varchar(20) default 'unfinished'")
    private String testingStatus = "unfinished";

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
    public static UserMixtureTask createTask(String taskId, String taskCompany, Long estBy, 
                                           String projectId, Long mixratioId, Long specimenId,
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
        task.setPrepareStatus("unfinished");
        task.setMakingStatus("unfinished");
        task.setTestingStatus("unfinished");
        // 注意: 这里不设置dueDate，因为需要从Project获取
        return task;
    }
    
    /**
     * 向后兼容的创建方法 (不含任务名称)
     */
    public static UserMixtureTask createTask(String taskId, String taskCompany, Long estBy, 
                                           String projectId, Long mixratioId, Long specimenId,
                                           String taskAssignment, String remarks) {
        return createTask(taskId, taskCompany, estBy, projectId, mixratioId, specimenId, 
                        taskAssignment, remarks, null);
    }

    /**
     * 设置截止日期
     * @param dueDate 截止日期
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * 获取截止日期
     * @return 截止日期
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * 设置接受人
     * @param acceptor 接受人
     */
    public void setAcceptor(String acceptor) {
        this.acceptor = acceptor;
    }

    /**
     * 获取接受人
     * @return 接受人
     */
    public String getAcceptor() {
        return acceptor;
    }

    /**
     * 设置接受时间
     * @param acceptTime 接受时间
     */
    public void setAcceptTime(Long acceptTime) {
        this.acceptTime = acceptTime;
    }

    /**
     * 获取接受时间
     * @return 接受时间
     */
    public Long getAcceptTime() {
        return acceptTime;
    }
    
    /**
     * 设置准备状态
     * @param prepareStatus 准备状态
     */
    public void setPrepareStatus(String prepareStatus) {
        this.prepareStatus = prepareStatus;
    }
    
    /**
     * 获取准备状态
     * @return 准备状态
     */
    public String getPrepareStatus() {
        return prepareStatus;
    }
    
    /**
     * 设置制作状态
     * @param makingStatus 制作状态
     */
    public void setMakingStatus(String makingStatus) {
        this.makingStatus = makingStatus;
    }
    
    /**
     * 获取制作状态
     * @return 制作状态
     */
    public String getMakingStatus() {
        return makingStatus;
    }
    
    /**
     * 设置测试状态
     * @param testingStatus 测试状态
     */
    public void setTestingStatus(String testingStatus) {
        this.testingStatus = testingStatus;
    }
    
    /**
     * 获取测试状态
     * @return 测试状态
     */
    public String getTestingStatus() {
        return testingStatus;
    }
}
