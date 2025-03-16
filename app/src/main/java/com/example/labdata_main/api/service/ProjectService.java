package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.ProjectNameResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * 项目服务接口，用于与后端项目相关API进行交互
 */
public interface ProjectService {
    /**
     * 通过项目ID获取项目名称
     * 
     * @param projectId 项目ID
     * @return API响应，包含项目名称信息
     */
    @GET("/api/projects/{projectId}/name")
    Call<ApiResponse<ProjectNameResponse>> getProjectNameById(@Path("projectId") Long projectId);
}
