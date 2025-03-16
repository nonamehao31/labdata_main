package com.example.labdata_main.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * 项目名称响应对象，用于接收项目名称查询API的响应
 */
public class ProjectNameResponse {
    @SerializedName("projectId")
    private Long projectId;
    
    @SerializedName("projectName")
    private String projectName;

    public ProjectNameResponse() {
    }

    public ProjectNameResponse(Long projectId, String projectName) {
        this.projectId = projectId;
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
    
    @Override
    public String toString() {
        return "ProjectNameResponse{" +
                "projectId=" + projectId +
                ", projectName='" + projectName + '\'' +
                '}';
    }
}
