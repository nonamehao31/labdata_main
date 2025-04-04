package com.example.labdata_main.api.service;

import com.example.labdata_main.api.model.ApiResponse;
import com.example.labdata_main.api.model.SpecimenParametersResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * 试件相关API接口
 */
public interface SpecimenService {
    /**
     * 获取制件参数
     * @param specimenId 制件方法ID
     * @return 制件参数响应
     */
    @GET("api/specimens/{id}/parameters")
    Call<ApiResponse<SpecimenParametersResponse>> getSpecimenParameters(@Path("id") Long specimenId);
}
