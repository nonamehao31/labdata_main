package com.example.labdata.payload.response;

/**
 * 项目名称响应类，用于通过项目ID查询项目名称的API响应
 */
public class ProjectNameResponse {
    private Long projectId;
    private String projectName;

    public ProjectNameResponse() {
    }

    public ProjectNameResponse(Long projectId, String projectName) {
        this.projectId = projectId;
        this.projectName = projectName;
    }
    
    /**
     * 只有项目名称的构造函数
     * 
     * @param projectName 项目名称
     */
    public ProjectNameResponse(String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
