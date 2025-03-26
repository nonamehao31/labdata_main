package com.example.labdata_main.api;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.AsphaltDetailResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * 沥青实验任务API接口
 */
public interface AsphaltTaskApi {

    /**
     * 根据任务ID获取沥青任务详情
     *
     * @param taskId 任务ID
     * @return API响应，包含沥青任务详情
     */
    @GET("api/asphalt/experiments/detail/{taskId}")
    Call<ApiResponse<AsphaltDetailResponse>> getAsphaltDetailByTaskId(@Path("taskId") String taskId);
}
