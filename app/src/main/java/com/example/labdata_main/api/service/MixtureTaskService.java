package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.MixtureTaskResponse;
import com.example.labdata_main.api.model.ProjectNameResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 混合料任务服务接口
 */
public interface MixtureTaskService {
    
    /**
     * 获取所有混合料任务
     * 注意：此方法直接返回数组，不使用ApiResponse包装
     */
    @GET("api/mixtureTask/list")
    Call<List<MixtureTaskResponse>> getAllMixtureTasks();
    
    /**
     * 根据任务类型获取混合料任务
     * 注意：此方法直接返回数组，不使用ApiResponse包装
     */
    @GET("api/mixtureTask/listByType")
    Call<List<MixtureTaskResponse>> getMixtureTasksByType(@Query("taskType") String taskType);
    
    /**
     * 获取用户的混合料任务
     * 注意：此方法返回ApiResponse包装的数组
     */
    @GET("api/mixture-tasks/company")
    Call<ApiResponse<List<MixtureTaskResponse>>> getUserMixtureTasks(@Query("companyId") String companyId);
    
    /**
     * 根据任务ID获取项目名称
     */
    @GET("api/mixtureTask/{taskId}/projectName")
    Call<ProjectNameResponse> getProjectNameByTaskId(@Path("taskId") Long taskId);
}
