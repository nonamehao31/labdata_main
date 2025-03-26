package com.example.labdata_main.api;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MixtureTaskModel;
import com.example.labdata_main.model.MixratioAndCompactionResponse;
import com.example.labdata_main.model.TaskAssignmentResponse;

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
}
