package com.example.labdata.payload.response;

import java.time.Instant;

/**
 * 项目响应数据传输对象
 */
public class ProjectResponse {
    
    private Long id;
    private String projectId;
    private String name;
    private String description;
    private String deadline;
    private String companyId;
    private String createdBy;
    private Instant createdDate;
    private Instant updatedDate;

    /**
     * 无参构造函数
     */
    public ProjectResponse() {
    }

    /**
     * 全参数构造函数
     *
     * @param id          项目ID
     * @param projectId   项目唯一标识符
     * @param name        项目名称
     * @param description 项目描述
     * @param deadline    截止日期
     * @param companyId   公司ID
     * @param createdBy   创建者
     * @param createdDate 创建日期
     * @param updatedDate 更新日期
     */
    public ProjectResponse(Long id, String projectId, String name, String description, String deadline,
                          String companyId, String createdBy, Instant createdDate, Instant updatedDate) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.companyId = companyId;
        this.createdBy = createdBy;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
}
