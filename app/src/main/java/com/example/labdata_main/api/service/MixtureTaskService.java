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
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Body;

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
    
    /**
     * 根据任务ID前缀获取所有具有相同前缀的任务状态
     * 
     * @param taskIdPrefix 任务ID前缀
     * @return 任务状态列表
     */
    @GET("api/mixture-tasks/status/{taskIdPrefix}")
    Call<ApiResponse<List<Map<String, String>>>> getTaskStatusByPrefix(@Path("taskIdPrefix") String taskIdPrefix);
    
    /**
     * 根据任务ID前缀获取试件制作方法和配比信息
     *
     * @param taskIdPrefix 任务ID前缀
     * @return 包含配比和试件制作方法的列表
     */
    @GET("api/mixture-tasks/specimen-methods/{taskIdPrefix}")
    Call<ApiResponse<List<Map<String, Object>>>> getSpecimenMethodsByTaskPrefix(@Path("taskIdPrefix") String taskIdPrefix);
    
    /**
     * 提交设备信息
     * 
     * @param taskId 任务ID
     * @param deviceType 设备类型
     * @param deviceModel 设备型号
     * @param manufacturer 设备厂家（可选）
     * @return 设备信息和类型
     */
    @PUT("api/mixtureTask/{taskId}/device")
    Call<ApiResponse<Map<String, String>>> saveDeviceInfo(
        @Path("taskId") String taskId,
        @Query("deviceType") String deviceType,
        @Query("deviceModel") String deviceModel,
        @Query("manufacturer") String manufacturer
    );
    
    /**
     * 获取试件制备所需的方法、配比和设备信息
     */
    @GET("api/mixture-tasks/{taskId}/specimen-data")
    Call<ApiResponse<Map<String, Object>>> getSpecimenData(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取任务详细信息
     */
    @GET("api/mixtureTask/{taskId}")
    Call<ApiResponse<Map<String, Object>>> getMixtureTaskByTaskId(@Path("taskId") String taskId);
    
    /**
     * 更新任务的制件状态为"已完成"
     *
     * @param taskIdPrefix 任务ID前缀
     * @return 更新结果
     */
    @PUT("api/mixture-tasks/{taskIdPrefix}/making_status/finished")
    Call<ApiResponse<Boolean>> updateMakingStatusToFinished(@Path("taskIdPrefix") String taskIdPrefix);
    
    /**
     * 完成材料准备
     * 
     * @param taskId 任务ID
     * @param prepareTime 备料时间
     * @return 更新结果
     */
    @PUT("api/mixture-tasks/{taskId}/material-preparation")
    Call<ApiResponse<Boolean>> completeMaterialPreparation(
        @Path("taskId") Long taskId,
        @Query("prepareTime") Long prepareTime
    );
    
    /**
     * 保存马歇尔试验数据
     * 
     * @param requestData 包含任务ID和试验数据的请求体
     * @return 保存结果
     */
    @POST("api/marshall-test")
    Call<ApiResponse<Map<String, Object>>> saveMarshallTest(
        @Body Map<String, Object> requestData
    );
    
    /**
     * 保存汉堡车辙实验数据
     * 
     * @param requestData 包含任务ID和车辙实验数据的请求体
     * @return 保存结果
     */
    @POST("api/hamburg-rutting-test")
    Call<ApiResponse<Map<String, Object>>> saveHamburgRuttingTest(
        @Body Map<String, Object> requestData
    );
    
    /**
     * 保存沥青混合料弯曲试验数据
     * 
     * @param requestData 包含任务ID、配比ID、跨径长度和试件数据的请求体
     * @return 保存结果
     */
    @POST("api/mixture-bending-test")
    Call<ApiResponse<Map<String, Object>>> saveMixtureBendingTest(
        @Body Map<String, Object> requestData
    );
    
    /**
     * 获取沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 试验数据
     */
    @GET("api/mixture-bending-test/task/{taskId}/mix-ratio/{mixRatioId}")
    Call<ApiResponse<Map<String, Object>>> getMixtureBendingTestByTaskIdAndMixRatioId(
            @Path("taskId") String taskId,
            @Path("mixRatioId") Long mixRatioId
    );

    /**
     * 获取沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @return 试验数据列表
     */
    @GET("api/mixture-bending-test/task/{taskId}")
    Call<ApiResponse<List<Map<String, Object>>>> getMixtureBendingTestsByTaskId(
            @Path("taskId") String taskId
    );
    
    /**
     * 更新任务的设备信息（使用JSON格式）
     * 
     * @param requestBody 包含任务ID和设备信息的JSON字符串
     * @return 更新结果
     */
    @POST("api/mixtureTask/equipment")
    Call<ApiResponse<Boolean>> updateTaskEquipment(
        @Body String requestBody
    );
    
    /**
     * 保存动态模量试验数据
     */
    @POST("api/mixtureTask/saveDynamicModulusTest")
    Call<ApiResponse<Map<String, Object>>> saveDynamicModulusTest(@Body Map<String, Object> requestData);

    /**
     * 保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据
     * 
     * @param requestData 包含任务ID、配比ID和试件数据的请求体
     * @return 保存结果
     */
    @POST("api/mixtureTask/saveDirectStretchingFatigueTestData")
    Call<ApiResponse<Map<String, String>>> saveDirectStretchingFatigueTestData(@Body Map<String, Object> requestData);

    /**
     * 保存四点弯曲疲劳寿命试验数据
     * 
     * @param requestData 包含任务ID、配比ID和试件数据的请求体
     * @return 保存结果
     */
    @POST("api/mixtureTask/saveFourPointFatigueTestData")
    Call<ApiResponse<Map<String, String>>> saveFourPointFatigueTestData(@Body Map<String, Object> requestData);

    /**
     * 保存单轴压缩试验数据
     * 
     * @param requestData 包含任务ID、配比ID和试件数据的请求体
     * @return 保存结果
     */
    @POST("api/mixtureTask/saveUniaxialCompressionTestData")
    Call<ApiResponse<Map<String, String>>> saveUniaxialCompressionTestData(@Body Map<String, Object> requestData);

    /**
     * 保存劈裂试验数据
     * 
     * @param requestData 包含任务ID、配比ID和试件数据的请求体
     * @return 保存结果
     */
    @POST("api/mixtureTask/saveSplittingTestData")
    Call<ApiResponse<Map<String, String>>> saveSplittingTestData(@Body Map<String, Object> requestData);

    /**
     * 获取混合料任务的测试状态
     * 
     * @param taskId 任务ID
     * @return 实验状态（测试状态）
     */
    @GET("api/mixture-tasks/{taskId}/testing-status")
    Call<ApiResponse<String>> getTestingStatus(@Path("taskId") String taskId);

    /**
     * 获取混合料任务中各实验类型的状态
     * 
     * @param taskId 任务ID
     * @return 任务状态列表，包含该前缀下所有相关任务的状态
     */
    @GET("api/mixture-tasks/{taskId}/experiment-type-status")
    Call<ApiResponse<List<Map<String, String>>>> getExperimentTypeStatus(@Path("taskId") String taskId);
}
