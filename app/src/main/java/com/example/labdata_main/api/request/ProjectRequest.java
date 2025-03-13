package com.example.labdata_main.api.request;

/**
 * 项目请求类，用于创建新项目
 */
public class ProjectRequest {
    private String name;
    private String description;
    private String deadline;
    private String companyId;

    /**
     * 无参构造函数
     */
    public ProjectRequest() {
    }

    /**
     * 全参数构造函数
     * 
     * @param name        项目名称
     * @param description 项目描述
     * @param deadline    截止日期
     * @param companyId   公司ID
     */
    public ProjectRequest(String name, String description, String deadline, String companyId) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.companyId = companyId;
    }

    // Getters and Setters
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
}
