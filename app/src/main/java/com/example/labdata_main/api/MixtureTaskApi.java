package com.example.labdata_main.api;

import com.example.labdata_main.model.MixtureTaskModel;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MixtureTaskApi {
    @GET("/api/mixtureTask/list")
    Call<List<MixtureTaskModel>> getAllMixtureTasks();
    
    @GET("/api/mixtureTask/listByType")
    Call<List<MixtureTaskModel>> getMixtureTasksByType(@Query("taskType") String taskType);
}
