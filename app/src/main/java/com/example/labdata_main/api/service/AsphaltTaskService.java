package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.AsphaltDetailResponse;
import com.example.labdata_main.api.request.DuctilityTestRequest;
import com.example.labdata_main.api.request.PenetrationTestRequest;
import com.example.labdata_main.api.request.SofteningPointTestRequest;
import com.example.labdata_main.api.request.BrookfieldViscosityTestRequest;
import com.example.labdata_main.api.request.DynamicShearRheometerTestRequest;
import com.example.labdata_main.api.request.BbrTestRequest;
import com.example.labdata_main.api.model.AsphaltTaskResponse;
import com.example.labdata_main.api.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;
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


    /**
     * 提交针入度试验数据
     * @param request 软化点试验数据请求
     * @return API响应
     */
    @POST("api/asphalt/penetration")
    Call<ApiResponse<Boolean>> submitPenetrationTest(@Body PenetrationTestRequest request);

    /**
     * 提交软化点试验数据
     * @param request 软化点试验数据请求
     * @return API响应
     */
    @POST("api/softening-point/submit")
    Call<ApiResponse<Boolean>> submitSofteningPointTest(@Body SofteningPointTestRequest request);

    /**
     * 提交延度试验数据
     * @param request 延度试验数据请求
     * @return API响应
     */
    @POST("api/ductility/submit")
    Call<ApiResponse<Boolean>> submitDuctilityTest(@Body DuctilityTestRequest request);


    /**
     * 提交布鲁克菲尔德试验数据
     * @param request 延度试验数据请求
     * @return API响应
     */
    @POST("api/brookfield-viscosity/submit")
    Call<ApiResponse<Boolean>> submitBrookfieldViscosityTest(@Body BrookfieldViscosityTestRequest request);
    
    /**
     * 提交沥青弯曲蠕变劲度试验（弯曲梁流变仪法）数据
     * @param request 弯曲梁流变仪试验数据请求
     * @return API响应
     */
    @POST("api/bbr/submit")
    Call<ApiResponse<Boolean>> submitBbrTest(@Body BbrTestRequest request);

    /**
     * 提交动态剪切流变仪实验数据
     * @param request 动态剪切流变仪实验数据请求
     * @return API响应
     */
    @POST("api/dsr/submit")
    Call<ApiResponse<Boolean>> submitDynamicShearRheometerTest(@Body DynamicShearRheometerTestRequest request);
    
    /**
     * 更新实验任务状态为已完成
     * @param taskId 任务ID
     * @param experimentType 实验类型
     * @return API响应
     */
    @POST("api/asphalt/experiments/updateExperimentStatus/{taskId}")
    Call<ApiResponse<Boolean>> updateExperimentStatus(
            @Path("taskId") String taskId,
            @Query("experimentType") String experimentType);
    
    /**
     * 获取实验任务状态
     * @param taskId 任务ID
     * @return API响应
     */
    @GET("api/asphalt/experiments/experimentStatus/{taskId}")
    Call<ApiResponse<String>> getExperimentStatus(@Path("taskId") String taskId);
}
