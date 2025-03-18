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
import retrofit2.http.PUT;
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
    
    /**
     * 接受任务，将任务状态从CREATED更新为ONGOING
     * 
     * @param taskId 任务ID
     * @param acceptor 接受人
     * @param acceptTime 接受时间
     * @return 更新结果
     */
    @PUT("api/mixture-tasks/{taskId}/accept")
    Call<ApiResponse<Boolean>> acceptTask(
        @Path("taskId") Long taskId,
        @Query("acceptor") String acceptor,
        @Query("acceptTime") Long acceptTime
    );

    /**
     * 更新任务的备料状态为"已完成"
     *
     * @param taskId 任务ID
     * @return 更新结果
     */
    @PUT("api/mixture-tasks/{taskId}/prepare_status")
    Call<ApiResponse<Boolean>> updatePrepareStatus(
            @Path("taskId") String taskId
    );
    
    /**
     * 获取任务的备料状态
     *
     * @param taskId 任务ID
     * @return 任务备料状态
     */
    @GET("api/mixture-tasks/{taskId}/prepare_status")
    Call<ApiResponse<String>> getPrepareStatus(
            @Path("taskId") String taskId
    );
}
