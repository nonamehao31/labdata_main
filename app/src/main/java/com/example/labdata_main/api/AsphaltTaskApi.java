package com.example.labdata_main.api;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.AsphaltDetailResponse;
import com.example.labdata_main.model.AsphaltTaskAssignmentResponse;
import com.example.labdata_main.model.BbrTestResponse;
import com.example.labdata_main.model.BrookfieldViscosityResponse;
import com.example.labdata_main.model.DirectStretchingFatigueTestResponse;
import com.example.labdata_main.model.DsrTestResponse;
import com.example.labdata_main.model.DuctilityTestResponse;
import com.example.labdata_main.model.DynamicModulusTestResponse;
import com.example.labdata_main.model.FourPointBendingFatigueTestResponse;
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
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含沥青任务详情
     */
    @GET("api/asphalt/experiments/detail/{asphaltExperimentId}")
    Call<ApiResponse<AsphaltDetailResponse>> getAsphaltDetailByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取针入度实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含针入度实验数据
     */
    @GET("api/asphalt/penetration/{asphaltExperimentId}")
    Call<ApiResponse<PenetrationTestResponse>> getPenetrationTestByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取软化点实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含软化点实验数据
     */
    @GET("api/softening-point/{asphaltExperimentId}")
    Call<ApiResponse<SofteningPointResponse>> getSofteningPointByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取延度实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含延度实验数据
     */
    @GET("api/ductility/{asphaltExperimentId}")
    Call<ApiResponse<DuctilityTestResponse>> getDuctilityTestByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取布鲁克菲尔德旋转黏度实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return 布鲁克菲尔德旋转黏度实验数据列表
     */
    @GET("api/brookfield-viscosity/task/{asphaltExperimentId}")
    Call<List<BrookfieldViscosityResponse>> getBrookfieldViscosityByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取弯曲梁流变仪(BBR)实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含弯曲梁流变仪实验数据列表
     */
    @GET("api/bbr/task/{asphaltExperimentId}")
    Call<ApiResponse<List<BbrTestResponse>>> getBbrTestByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取动态剪切流变仪(DSR)实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含动态剪切流变仪实验数据列表
     */
    @GET("api/dsr/task/{asphaltExperimentId}")
    Call<ApiResponse<List<DsrTestResponse>>> getDsrTestByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取动态模量实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含动态模量实验数据列表
     */
    @GET("api/dynamic-modulus/task/{asphaltExperimentId}")
    Call<ApiResponse<List<DynamicModulusTestResponse>>> getDynamicModulusByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据列表
     */
    @GET("api/direct-stretching-fatigue/task/{asphaltExperimentId}")
    Call<ApiResponse<List<DirectStretchingFatigueTestResponse>>> getDirectStretchingFatigueByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);
    
    /**
     * 根据任务ID获取沥青混合料四点弯曲疲劳寿命实验数据
     *
     * @param asphaltExperimentId 任务ID
     * @return API响应，包含沥青混合料四点弯曲疲劳寿命实验数据
     */
    @GET("api/mixtureTask/getFourPointBendingTest/{asphaltExperimentId}")
    Call<ApiResponse<FourPointBendingFatigueTestResponse>> getFourPointBendingTestByTaskId(@Path("asphaltExperimentId") String asphaltExperimentId);

    /**
     * 根据任务ID获取沥青实验任务指派信息
     *
     * @param asphaltExperimentId 沥青实验ID
     * @return API响应，包含沥青实验任务指派信息
     */
    @GET("api/asphalt/experiments/task-assignment/{asphaltExperimentId}")
    Call<ApiResponse<AsphaltTaskAssignmentResponse>> getTaskAssignment(@Path("asphaltExperimentId") String asphaltExperimentId);
}
