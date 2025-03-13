package com.example.labdata.model;

import com.example.labdata.model.audit.DateAudit;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 项目实体类
 */
@Entity
@Table(name = "projects")
public class Project extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "project_name", nullable = false)
    private String name;
    
    @Column(name = "name", nullable = false)
    private String nameField;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_due")
    private String deadline;

    @Column(name = "project_company", nullable = false)
    private String companyId;
    
    @Column(name = "company_id", nullable = false)
    private String companyIdDuplicate;

    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "project_est_time")
    private String estimatedTime; // 改为字符串类型，存储人类可读的日期时间格式
    
    @Column(name = "project_id", unique = true)
    private String projectId;
    
    // 用于格式化日期时间的静态常量
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 无参构造函数
     */
    public Project() {
    }

    /**
     * 全参数构造函数
     *
     * @param name        项目名称
     * @param description 项目描述
     * @param deadline    截止日期
     * @param companyId   公司ID
     * @param createdBy   创建者
     */
    public Project(String name, String description, String deadline, String companyId, String createdBy) {
        this.name = name;
        this.nameField = name; // 同时设置name列
        this.description = description;
        this.deadline = deadline;
        this.companyId = companyId;
        this.companyIdDuplicate = companyId; // 同时设置到两个字段
        this.createdBy = createdBy;
        
        // 设置估计时间为当前时间的人类可读格式
        this.estimatedTime = LocalDateTime.now()
                .format(DATE_TIME_FORMATTER);
        
        // 生成项目ID: 日期+公司ID+数字后缀
        // 实际ID生成将在服务层处理
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.nameField = name; // 同步更新两个name字段
    }
    
    public String getNameField() {
        return nameField;
    }
    
    public void setNameField(String nameField) {
        this.nameField = nameField;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
        this.companyIdDuplicate = companyId; // 同步更新两个字段
    }

    public String getCompanyIdDuplicate() {
        return companyIdDuplicate;
    }

    public void setCompanyIdDuplicate(String companyIdDuplicate) {
        this.companyIdDuplicate = companyIdDuplicate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(String estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
    
    /**
     * 设置估计时间，从Instant对象转换为人类可读的格式
     * 
     * @param instant 时间点
     */
    public void setEstimatedTime(Instant instant) {
        if (instant != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            this.estimatedTime = dateTime.format(DATE_TIME_FORMATTER);
        } else {
            this.estimatedTime = null;
        }
    }
    
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
