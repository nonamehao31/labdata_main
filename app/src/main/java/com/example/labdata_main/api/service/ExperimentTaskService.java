package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.model.ExperimentTask;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 实验任务服务接口
 * 用于处理与实验任务相关的API请求
 */
public interface ExperimentTaskService {
    
    /**
     * 获取所有实验任务
     * @return 实验任务列表
     */
    @GET("api/tasks")
    Call<ApiResponse<List<ExperimentTask>>> getAllTasks();
    
    /**
     * 按类型获取实验任务
     * @param type 实验类型
     * @return 实验任务列表
     */
    @GET("api/tasks/type")
    Call<ApiResponse<List<ExperimentTask>>> getTasksByType(@Query("type") String type);
    
    /**
     * 获取指定用户或公司的实验任务
     * @param companyId 公司ID
     * @return 实验任务列表
     */
    @GET("api/tasks/company")
    Call<ApiResponse<List<ExperimentTask>>> getTasksByCompany(@Query("companyId") String companyId);
    
    /**
     * 获取指定实验任务的详情
     * @param taskId 任务ID
     * @return 实验任务详情
     */
    @GET("api/tasks/{taskId}")
    Call<ApiResponse<ExperimentTask>> getTaskById(@Path("taskId") String taskId);
    
    /**
     * 创建新的实验任务
     * @param task 实验任务详情
     * @return 创建结果
     */
    @POST("api/tasks")
    Call<ApiResponse<ExperimentTask>> createTask(@Body ExperimentTask task);
    
    /**
     * 更新实验任务
     * @param taskId 任务ID
     * @param task 实验任务详情
     * @return 更新结果
     */
    @PUT("api/tasks/{taskId}")
    Call<ApiResponse<ExperimentTask>> updateTask(@Path("taskId") String taskId, @Body ExperimentTask task);
}
