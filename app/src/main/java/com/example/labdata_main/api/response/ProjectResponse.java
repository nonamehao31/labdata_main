package com.example.labdata_main.api.response;

import java.util.Date;

/**
 * 项目响应类，用于接收服务器返回的项目信息
 */
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String deadline;
    private String companyId;
    private String createdBy;
    private Date createdDate;
    private Date updatedDate;

    /**
     * 无参构造函数
     */
    public ProjectResponse() {
    }

    /**
     * 全参数构造函数
     *
     * @param id          项目ID
     * @param name        项目名称
     * @param description 项目描述
     * @param deadline    截止日期
     * @param companyId   公司ID
     * @param createdBy   创建者
     * @param createdDate 创建日期
     * @param updatedDate 更新日期
     */
    public ProjectResponse(Long id, String name, String description, String deadline, 
                          String companyId, String createdBy, Date createdDate, Date updatedDate) {
        this.id = id;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }
}
