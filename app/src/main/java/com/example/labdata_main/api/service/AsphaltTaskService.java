package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.AsphaltDetailResponse;
import com.example.labdata_main.api.model.AsphaltTaskResponse;
import com.example.labdata_main.api.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 沥青任务服务接口
 */
public interface AsphaltTaskService {
    
    /**
     * 获取所有沥青实验任务
     */
    @GET("api/asphalt/experiments")
    Call<ApiResponse<List<AsphaltTaskResponse>>> getAllAsphaltExperiments();
    
    /**
     * 根据ID获取沥青实验任务
     */
    @GET("api/asphalt/experiments/{id}")
    Call<ApiResponse<AsphaltTaskResponse>> getAsphaltExperimentById(@Path("id") Long id);
    
    /**
     * 根据类型获取沥青实验任务
     */
    @GET("api/asphalt/experiments/type/{type}")
    Call<ApiResponse<List<AsphaltTaskResponse>>> getAsphaltExperimentsByType(@Path("type") String type);
    
    /**
     * 获取用户公司的所有沥青实验任务
     * 注意：这个方法使用的是/byCompany端点，它返回的响应会被映射到AsphaltTaskResponse
     * 但后端实际返回的是AsphaltExperimentResponse，可能导致字段不匹配
     */
    @GET("api/asphalt/experiments/byCompany")
    Call<ApiResponse<List<AsphaltTaskResponse>>> getUserAsphaltTasks(@Query("companyId") String companyId);
    
    /**
     * 获取沥青任务详情信息，包括沥青信息和实验指派信息
     *
     * @param taskId 任务ID
     * @return 包含沥青信息和实验指派信息的响应
     */
    @GET("api/asphalt/experiments/detail/{taskId}")
    Call<ApiResponse<AsphaltDetailResponse>> getAsphaltDetailByTaskId(@Path("taskId") String taskId);
    
    /**
     * 接受沥青任务
     *
     * @param taskId 任务ID
     * @param acceptor 接受者
     * @param acceptTime 接受时间
     * @return 操作结果响应
     */
    @POST("api/asphalt/experiments/accept/{taskId}")
    Call<ApiResponse<Boolean>> acceptAsphaltTask(
            @Path("taskId") String taskId,
            @Query("acceptor") String acceptor,
            @Query("acceptTime") Long acceptTime);
}
