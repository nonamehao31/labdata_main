package com.example.labdata_main.api;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.AsphaltDetailResponse;
import com.example.labdata_main.model.BbrTestResponse;
import com.example.labdata_main.model.BrookfieldViscosityResponse;
import com.example.labdata_main.model.DsrTestResponse;
import com.example.labdata_main.model.DuctilityTestResponse;
import com.example.labdata_main.model.PenetrationTestResponse;
import com.example.labdata_main.model.SofteningPointResponse;

import java.util.List;

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
    
    /**
     * 根据任务ID获取针入度实验数据
     *
     * @param taskId 任务ID
     * @return API响应，包含针入度实验数据
     */
    @GET("api/asphalt/penetration/{taskId}")
    Call<ApiResponse<PenetrationTestResponse>> getPenetrationTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取软化点实验数据
     *
     * @param taskId 任务ID
     * @return API响应，包含软化点实验数据
     */
    @GET("api/softening-point/{taskId}")
    Call<ApiResponse<SofteningPointResponse>> getSofteningPointByTaskId(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取延度实验数据
     *
     * @param taskId 任务ID
     * @return API响应，包含延度实验数据
     */
    @GET("api/ductility/{taskId}")
    Call<ApiResponse<DuctilityTestResponse>> getDuctilityTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取布鲁克菲尔德旋转黏度实验数据
     *
     * @param taskId 任务ID
     * @return 布鲁克菲尔德旋转黏度实验数据列表
     */
    @GET("api/brookfield-viscosity/task/{taskId}")
    Call<List<BrookfieldViscosityResponse>> getBrookfieldViscosityByTaskId(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取弯曲梁流变仪(BBR)实验数据
     *
     * @param taskId 任务ID
     * @return API响应，包含弯曲梁流变仪实验数据列表
     */
    @GET("api/bbr/task/{taskId}")
    Call<ApiResponse<List<BbrTestResponse>>> getBbrTestByTaskId(@Path("taskId") String taskId);
    
    /**
     * 根据任务ID获取动态剪切流变仪(DSR)实验数据
     *
     * @param taskId 任务ID
     * @return API响应，包含动态剪切流变仪实验数据列表
     */
    @GET("api/dsr/task/{taskId}")
    Call<ApiResponse<List<DsrTestResponse>>> getDsrTestByTaskId(@Path("taskId") String taskId);
}
