package com.example.labdata_main.api;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MarshallTestResponse;
import com.example.labdata_main.model.HamburgRuttingTestResponse;
import com.example.labdata_main.model.MixtureBendingTestResponse;
import com.example.labdata_main.model.MixtureTaskModel;
import com.example.labdata_main.model.MixratioAndCompactionResponse;
import com.example.labdata_main.model.TaskAssignmentResponse;
import com.example.labdata_main.model.DynamicModulusTestResponse;
import com.example.labdata_main.model.DirectStretchingFatigueTestResponse;
import com.example.labdata_main.model.FourPointBendingFatigueTestResponse;
import com.example.labdata_main.model.UniaxialCompressionTestResponse;
import com.example.labdata_main.model.SplittingTestResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface MixtureTaskApi {
    @GET("api/mixtureTask/list")
    Call<List<MixtureTaskModel>> getAllMixtureTasks();
    
    @GET("api/mixtureTask/listByType")
    Call<List<MixtureTaskModel>> getMixtureTasksByType(@Query("taskType") String taskType);
    
    @POST("api/mixtureTask/saveSplittingTestData")
    Call<ApiResponse<Map<String, String>>> saveSplittingTestData(@Body Map<String, Object> data);
    
    @GET("api/mixtureTask/getTaskAssignment/{taskId}")
    Call<TaskAssignmentResponse> getTaskAssignment(@Path("taskId") String taskId);

    /**
     * 获取配比名称和压实方法信息
     * 
     * @param taskId 任务ID
     * @return 配比和压实方法响应
     */
    @GET("api/mixtureTask/getMixratioAndCompaction/{taskId}")
    Call<MixratioAndCompactionResponse> getMixratioAndCompaction(@Path("taskId") String taskId);
    
    /**
     * 获取马歇尔稳定度实验数据
     * 
     * @param taskId 任务ID
     * @return 马歇尔稳定度实验数据响应
     */
    @GET("api/mixtureTask/getMarshallTest/{taskId}")
    Call<List<MarshallTestResponse>> getMarshallTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取汉堡车辙实验数据
     * 
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据响应
     */
    @GET("api/mixtureTask/getHamburgRuttingTest/{taskId}")
    Call<List<HamburgRuttingTestResponse>> getHamburgRuttingTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取沥青混合料弯曲实验数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料弯曲实验数据响应
     */
    @GET("api/mixtureTask/getMixtureBendingTest/{taskId}")
    Call<List<MixtureBendingTestResponse>> getMixtureBendingTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取动态模量实验数据
     * 
     * @param taskId 任务ID
     * @return 动态模量实验数据响应
     */
    @GET("api/mixtureTask/getDynamicModulusTest/{taskId}")
    Call<ApiResponse<List<DynamicModulusTestResponse>>> getDynamicModulusTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据响应
     */
    @GET("api/mixtureTask/getDirectStretchingFatigueTest/{taskId}")
    Call<ApiResponse<List<DirectStretchingFatigueTestResponse>>> getDirectStretchingFatigueTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取沥青混合料四点弯曲疲劳寿命实验数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料四点弯曲疲劳寿命实验数据响应
     */
    @GET("api/mixtureTask/getFourPointBendingTest/{taskId}")
    Call<ApiResponse<FourPointBendingFatigueTestResponse>> getFourPointBendingTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 获取沥青混合料单轴压缩试验（圆柱体法）数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料单轴压缩试验（圆柱体法）数据响应
     */
    @GET("api/mixtureTask/getUniaxialCompressionTest/{taskId}")
    Call<ApiResponse<UniaxialCompressionTestResponse>> getUniaxialCompressionTestByTaskId(@Path("taskId") String taskId);

    /**
     * 获取沥青混合料劈裂试验数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料劈裂试验数据响应
     */
    @GET("api/mixtureTask/getSplittingTest/{taskId}")
    Call<ApiResponse<List<SplittingTestResponse>>> getSplittingTestByTaskId(@Path("taskId") String taskId);
}
