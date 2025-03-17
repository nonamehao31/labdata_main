package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.MixRatioDetailResponse;
import com.example.labdata_main.api.model.MixratioSpecimenPair;
import com.example.labdata_main.api.model.MixtureTaskResponse;
import com.example.labdata_main.api.model.ProjectNameResponse;

import java.util.List;
import java.util.Map;

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
    
    /**
     * 根据任务ID获取配比和制件方法组合
     * @param taskId 任务ID
     * @return 配比和制件方法组合列表
     */
    @GET("api/mixtureTask/{taskId}/pairs")
    Call<ApiResponse<List<MixratioSpecimenPair>>> getMixratioSpecimenPairsByTaskId(@Path("taskId") Long taskId);
    
    /**
     * 根据任务ID获取配比详细信息
     * 
     * @param taskId 任务ID
     * @return 配比详细信息列表
     */
    @GET("api/mixtureTask/mixratio-details/{taskId}")
    Call<ApiResponse<List<MixRatioDetailResponse>>> getMixRatioDetailsByTaskId(@Path("taskId") Long taskId);
    
    /**
     * 根据任务ID获取实验指派信息
     * 
     * @param taskId 任务ID
     * @return 配比ID到实验指派列表的映射
     */
    @GET("api/mixtureTask/task-assignments/{taskId}")
    Call<ApiResponse<Map<Long, List<String>>>> getTaskAssignmentsByTaskId(@Path("taskId") Long taskId);
    
    /**
     * 根据任务ID获取备注信息
     * 
     * @param taskId 任务ID
     * @return 备注信息
     */
    @GET("api/mixtureTask/task-remarks/{taskId}")
    Call<ApiResponse<String>> getTaskRemarksByTaskId(@Path("taskId") Long taskId);
}
