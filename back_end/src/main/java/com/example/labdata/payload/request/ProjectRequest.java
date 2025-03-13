package com.example.labdata.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 项目请求数据传输对象
 */
public class ProjectRequest {
    
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String name;
    
    private String description;
    
    private String deadline;
    
    @NotNull(message = "公司ID不能为空")
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
