package com.example.labdata_main.api.service;

import com.example.labdata_main.api.request.TestAsphaltMaterialRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.TestAsphaltMaterialResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * 沥青材料API服务接口
 */
public interface TestAsphaltMaterialService {
    
    /**
     * 获取所有沥青材料
     * @return 沥青材料列表
     */
    @GET("api/test-asphalt-materials")
    Call<ApiResponse<List<TestAsphaltMaterialResponse>>> getAllAsphaltMaterials();
    
    /**
     * 获取未过期的沥青材料
     * @return 未过期的沥青材料列表
     */
    @GET("api/test-asphalt-materials/active")
    Call<ApiResponse<List<TestAsphaltMaterialResponse>>> getActiveAsphaltMaterials();
    
    /**
     * 获取指定类型的沥青材料
     * @param catalog 沥青类型
     * @return 指定类型的沥青材料列表
     */
    @GET("api/test-asphalt-materials/catalog/{catalog}")
    Call<ApiResponse<List<TestAsphaltMaterialResponse>>> getAsphaltMaterialsByType(@Path("catalog") String catalog);
    
    /**
     * 保存沥青材料
     * @param request 沥青材料请求
     * @return 保存结果
     */
    @POST("api/test-asphalt-materials")
    Call<ApiResponse<TestAsphaltMaterialResponse>> saveAsphaltMaterial(@Body TestAsphaltMaterialRequest request);
    
    /**
     * 获取指定ID的沥青材料
     * @param id 沥青材料ID
     * @return 沥青材料信息
     */
    @GET("api/test-asphalt-materials/{id}")
    Call<ApiResponse<TestAsphaltMaterialResponse>> getAsphaltMaterialById(@Path("id") Long id);
}
