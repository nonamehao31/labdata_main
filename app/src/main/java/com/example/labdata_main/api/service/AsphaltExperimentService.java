package com.example.labdata_main.api.service;

import com.example.labdata_main.api.request.AsphaltExperimentRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.AsphaltExperimentResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * 沥青实验相关API服务
 */
public interface AsphaltExperimentService {
    
    /**
     * 创建沥青实验任务
     * @param request 沥青实验请求体
     * @return API响应
     */
    @POST("/api/asphalt/experiments")
    Call<ApiResponse<AsphaltExperimentResponse>> createAsphaltExperiment(@Body AsphaltExperimentRequest request);
    
    /**
     * 批量创建沥青实验任务
     * @param requests 沥青实验请求体列表
     * @return API响应
     */
    @POST("/api/asphalt/experiments/batch")
    Call<ApiResponse<List<AsphaltExperimentResponse>>> createAsphaltExperiments(@Body List<AsphaltExperimentRequest> requests);
    
    /**
     * 获取所有沥青实验任务
     * @return API响应
     */
    @GET("/api/asphalt/experiments")
    Call<ApiResponse<List<AsphaltExperimentResponse>>> getAsphaltExperiments();
}
