package com.example.labdata_main.api;

import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.model.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * 制件方法相关的API服务接口
 */
public interface CompactionMethodService {
    
    /**
     * 获取用户所属单位的制件方法列表
     * 这个API会根据当前认证用户的信息返回其所属单位的所有制件方法
     * 
     * @return 制件方法列表的API响应
     */
    @GET("api/compaction-methods/organization")
    Call<ApiResponse<List<MoldingMethod>>> getOrganizationCompactionMethods();
    
    /**
     * 根据单位ID获取制件方法列表
     * 
     * @param organizationId 单位ID
     * @return 制件方法列表的API响应
     */
    @GET("api/compaction-methods")
    Call<ApiResponse<List<MoldingMethod>>> getCompactionMethodsByOrganization(
            @Query("organizationId") Long organizationId);
}
