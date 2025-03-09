package com.example.labdata_main.api;

import com.example.labdata_main.api.dto.AsphaltExperimentRequest;
import com.example.labdata_main.api.dto.AsphaltExperimentResponse;
import com.example.labdata_main.api.dto.SyncExperimentTaskRequest;
import com.example.labdata_main.api.request.DeviceRequest;
import com.example.labdata_main.api.request.LoginRequest;
import com.example.labdata_main.api.request.RegisterRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.api.response.LoginResponse;
import com.example.labdata_main.api.response.SupportedDeviceResponse;
import com.example.labdata_main.model.ExperimentTask;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * API服务接口，定义所有的API请求方法
 */
public interface ApiService {
    
    /**
     * 用户登录
     * @param request 登录请求参数
     * @return 登录响应
     */
    @POST(ApiConfig.LOGIN_URL)
    Call<LoginResponse> login(@Body LoginRequest request);
    
    /**
     * 用户注册
     * @param request 注册请求参数
     * @return 注册响应
     */
    @POST(ApiConfig.REGISTER_URL)
    Call<ApiResponse<Void>> register(@Body RegisterRequest request);

    /**
     * 保存单个设备信息
     * @param request 设备请求参数
     * @return 设备响应
     */
    @POST(ApiConfig.SAVE_DEVICE_URL)
    Call<ApiResponse<DeviceResponse>> saveDevice(@Body DeviceRequest request);

    /**
     * 批量保存设备信息
     * @param requests 设备请求参数列表
     * @return API响应
     */
    @POST(ApiConfig.SAVE_DEVICES_BATCH_URL)
    Call<ApiResponse<List<DeviceResponse>>> saveDevicesBatch(@Body List<DeviceRequest> requests);

    /**
     * 获取指定公司的所有设备
     * @param companyId 公司ID
     * @return 设备响应列表
     */
    @GET(ApiConfig.GET_DEVICES_BY_COMPANY_URL)
    Call<ApiResponse<List<DeviceResponse>>> getDevicesByCompany(@Path("companyId") String companyId);

    /**
     * 获取当前用户的所有设备
     * @return 设备响应列表
     */
    @GET(ApiConfig.GET_USER_DEVICES_URL)
    Call<ApiResponse<List<DeviceResponse>>> getUserDevices();
    
    /**
     * 获取所有支持的设备类型信息
     * @return 支持设备响应列表
     */
    @GET(ApiConfig.GET_ALL_SUPPORTED_DEVICES_URL)
    Call<ApiResponse<List<SupportedDeviceResponse>>> getAllSupportedDevices();
    
    /**
     * 根据类型获取支持的设备
     * @param type 设备类型
     * @return 支持设备响应列表
     */
    @GET(ApiConfig.GET_SUPPORTED_DEVICES_BY_TYPE_URL)
    Call<ApiResponse<List<SupportedDeviceResponse>>> getSupportedDevicesByType(@Path("type") String type);
    
    /**
     * 获取特定类型的所有厂商
     * @param type 设备类型
     * @return 厂商名称列表
     */
    @GET(ApiConfig.GET_MANUFACTURERS_BY_TYPE_URL)
    Call<ApiResponse<List<String>>> getManufacturersByType(@Path("type") String type);
    
    /**
     * 获取特定类型和厂商的型号
     * @param type 设备类型
     * @param manufacturer 厂商名称
     * @return 型号列表
     */
    @GET(ApiConfig.GET_MODELS_BY_TYPE_AND_MANUFACTURER_URL)
    Call<ApiResponse<List<String>>> getModelsByTypeAndManufacturer(
            @Path("type") String type,
            @Path("manufacturer") String manufacturer);
            
    /**
     * 保存实验任务
     * @param experimentTask 实验任务对象
     * @return API响应
     */
    @POST(ApiConfig.SAVE_EXPERIMENT_TASK_URL)
    Call<ApiResponse<ExperimentTask>> saveExperimentTask(@Body ExperimentTask experimentTask);
    
    /**
     * 获取所有实验任务
     * @return 实验任务列表
     */
    @GET(ApiConfig.GET_EXPERIMENT_TASKS_URL)
    Call<ApiResponse<List<ExperimentTask>>> getExperimentTasks();
    
    /**
     * 根据ID获取实验任务
     * @param id 实验任务ID
     * @return 实验任务
     */
    @GET(ApiConfig.GET_EXPERIMENT_TASK_BY_ID_URL)
    Call<ApiResponse<ExperimentTask>> getExperimentTaskById(@Path("id") Long id);
    
    /**
     * 更新实验任务
     * @param id 实验任务ID
     * @param experimentTask 实验任务对象
     * @return API响应
     */
    @PUT(ApiConfig.UPDATE_EXPERIMENT_TASK_URL)
    Call<ApiResponse<ExperimentTask>> updateExperimentTask(@Path("id") Long id, @Body ExperimentTask experimentTask);
    
    /**
     * 同步实验任务（使用专用DTO对象）
     * @param request 同步请求对象
     * @return 同步后的实验任务
     */
    @POST(ApiConfig.SYNC_EXPERIMENT_TASK_URL)
    Call<ExperimentTask> syncExperimentTask(@Body SyncExperimentTaskRequest request);
    
    /**
     * 同步实验任务
     * @param experimentTask 实验任务对象
     * @return API响应
     * @deprecated 使用 {@link #syncExperimentTask(SyncExperimentTaskRequest)} 替代
     */
    @Deprecated
    @POST(ApiConfig.SYNC_EXPERIMENT_TASK_URL)
    Call<ApiResponse<ExperimentTask>> syncExperimentTaskLegacy(@Body ExperimentTask experimentTask);

    /**
     * 检查公司是否已初始化设备
     * @param companyId 公司ID
     * @return API响应，返回boolean值，表示公司是否已初始化设备
     */
    @GET(ApiConfig.CHECK_COMPANY_EQUIPMENT_INITIALIZED)
    Call<ApiResponse<Boolean>> checkCompanyEquipmentInitialized(@Path("companyId") String companyId);

    /**
     * 获取公司的设备列表
     * @param companyId 公司ID
     * @return API响应，返回公司的设备列表
     */
    @GET(ApiConfig.GET_DEVICES_BY_COMPANY_URL)
    Call<ApiResponse<List<DeviceResponse>>> getCompanyEquipment(@Path("companyId") String companyId);

    /**
     * 检查邮箱是否已经存在
     * @param email 邮箱地址
     * @return 检查结果
     */
    @GET(ApiConfig.CHECK_EMAIL_EXISTS_URL + "/{email}")
    Call<ApiResponse<Boolean>> checkEmailExists(@Path("email") String email);

    /**
     * 创建沥青实验任务
     * @param request 沥青实验请求参数
     * @return API响应
     */
    @POST(ApiConfig.CREATE_ASPHALT_TASK_URL)
    Call<ApiResponse<Long>> createAsphaltExperimentTask(@Body AsphaltExperimentRequest request);

    /**
     * 获取所有沥青实验任务
     * @return 沥青实验任务列表
     */
    @GET(ApiConfig.GET_ASPHALT_TASKS_URL)
    Call<ApiResponse<List<ExperimentTask>>> getAsphaltExperimentTasks();

    /**
     * 根据ID获取沥青实验任务
     * @param id 实验任务ID
     * @return 沥青实验响应
     */
    @GET(ApiConfig.GET_ASPHALT_TASK_BY_ID_URL)
    Call<ApiResponse<AsphaltExperimentResponse>> getAsphaltExperimentTaskById(@Path("id") Long id);

    /**
     * 更新沥青实验任务
     * @param id 任务ID
     * @param request 更新请求
     * @return API响应
     */
    @PUT(ApiConfig.UPDATE_ASPHALT_TASK_URL)
    Call<ApiResponse<Void>> updateAsphaltExperimentTask(@Path("id") Long id, @Body AsphaltExperimentRequest request);
}
