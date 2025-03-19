package com.example.labdata_main.api;

import com.example.labdata_main.api.dto.SyncExperimentTaskRequest;
import com.example.labdata_main.api.request.AsphaltMaterialRequest;
import com.example.labdata_main.api.request.DeviceRequest;
import com.example.labdata_main.api.request.LoginRequest;
import com.example.labdata_main.api.request.MixtureTaskRequest;
import com.example.labdata_main.api.request.ProjectRequest;
import com.example.labdata_main.api.request.RegisterRequest;
import com.example.labdata_main.api.request.SandMaterialRequest;
import com.example.labdata_main.api.request.StoneMaterialRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.AsphaltMaterialResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.api.response.LoginResponse;
import com.example.labdata_main.api.response.ProjectResponse;
import com.example.labdata_main.api.response.SandMaterialResponse;
import com.example.labdata_main.api.response.StoneMaterialResponse;
import com.example.labdata_main.api.response.SupportedDeviceResponse;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.MixtureTaskModel;
import com.example.labdata_main.model.SupportMixtureTaskModel;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

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
     * @return 设备响应列表
     */
    @POST(ApiConfig.SAVE_DEVICES_BATCH_URL)
    Call<ApiResponse<List<DeviceResponse>>> saveDevices(@Body List<DeviceRequest> requests);

    /**
     * 批量保存设备信息
     * @param requests 设备请求参数列表
     * @return 设备响应列表
     */
    @POST(ApiConfig.SAVE_DEVICES_BATCH_URL)
    Call<ApiResponse<List<DeviceResponse>>> saveDevicesBatch(@Body List<DeviceRequest> requests);

    /**
     * 获取特定公司的所有设备
     * @param companyId 公司ID
     * @return 设备响应列表
     */
    @GET(ApiConfig.GET_DEVICES_BY_COMPANY_URL)
    Call<ApiResponse<List<DeviceResponse>>> getDevicesByCompany(@Path("companyId") Long companyId);

    /**
     * 获取当前用户的所有设备
     * @return 设备响应列表
     */
    @GET(ApiConfig.GET_USER_DEVICES_URL)
    Call<ApiResponse<List<DeviceResponse>>> getUserDevices();

    /**
     * 检查公司设备初始化状态
     * @param companyId 公司ID
     * @return 初始化状态
     */
    @GET(ApiConfig.CHECK_COMPANY_EQUIPMENT_INITIALIZED)
    Call<ApiResponse<Boolean>> checkCompanyEquipmentInitialized(@Path("companyId") String companyId);

    /**
     * 获取公司设备列表
     * @param companyId 公司ID
     * @return 设备响应列表
     */
    @GET(ApiConfig.GET_COMPANY_EQUIPMENT_URL)
    Call<ApiResponse<List<DeviceResponse>>> getCompanyEquipment(@Path("companyId") String companyId);

    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 检查结果
     */
    @GET(ApiConfig.CHECK_EMAIL_EXISTS_URL)
    Call<ApiResponse<Boolean>> checkEmailExists(@Query("email") String email);

    /**
     * 获取所有支持的设备
     * @return 支持设备列表
     */
    @GET(ApiConfig.GET_ALL_SUPPORTED_DEVICES_URL)
    Call<ApiResponse<List<SupportedDeviceResponse>>> getAllSupportedDevices();

    /**
     * 获取特定类型的支持设备
     * @param type 设备类型
     * @return 支持设备列表
     */
    @GET(ApiConfig.GET_SUPPORTED_DEVICES_BY_TYPE_URL)
    Call<ApiResponse<List<SupportedDeviceResponse>>> getSupportedDevicesByType(@Path("type") String type);

    /**
     * 获取设备类型下可用的所有制造商
     * @param type 设备类型
     * @return 制造商列表
     */
    @GET(ApiConfig.GET_MANUFACTURERS_BY_TYPE_URL)
    Call<ApiResponse<List<String>>> getManufacturersByType(@Path("type") String type);

    /**
     * 获取设备类型和制造商下可用的所有型号
     * @param type 设备类型
     * @param manufacturer 制造商
     * @return 型号列表
     */
    @GET(ApiConfig.GET_MODELS_BY_TYPE_AND_MANUFACTURER_URL)
    Call<ApiResponse<List<String>>> getModelsByTypeAndManufacturer(
            @Path("type") String type,
            @Path("manufacturer") String manufacturer
    );

    /**
     * 保存实验任务
     * @param task 实验任务
     * @return 保存结果
     */
    @POST(ApiConfig.SAVE_EXPERIMENT_TASK_URL)
    Call<ApiResponse<ExperimentTask>> saveExperimentTask(@Body ExperimentTask task);

    /**
     * 同步实验任务
     * @param request 同步请求
     * @return 同步结果
     */
    @POST(ApiConfig.SYNC_EXPERIMENT_TASK_URL)
    Call<ApiResponse<ExperimentTask>> syncExperimentTask(@Body SyncExperimentTaskRequest request);

    /**
     * 批量同步实验任务
     * @param requests 同步请求列表
     * @return 同步结果
     */
    @POST(ApiConfig.SYNC_EXPERIMENT_TASKS_URL)
    Call<ApiResponse<List<ExperimentTask>>> syncExperimentTasks(@Body List<SyncExperimentTaskRequest> requests);

    /**
     * 创建新项目
     * @param request 项目请求参数
     * @return 项目响应
     */
    @POST(ApiConfig.PROJECT_URL)
    Call<ApiResponse<ProjectResponse>> createProject(@Body ProjectRequest request);
    
    /**
     * 使用原始JSON创建新项目
     * @param requestBody 包含项目信息的原始JSON请求体
     * @return 响应体
     */
    @POST(ApiConfig.PROJECT_URL)
    Call<ResponseBody> createProjectRaw(@Body RequestBody requestBody);

    /**
     * 获取当前用户的所有项目
     * @return 项目列表
     */
    @GET(ApiConfig.PROJECT_URL)
    Call<ApiResponse<List<ProjectResponse>>> getProjects();

    /**
     * 通过ID获取项目详情
     * @param id 项目ID
     * @return 项目详情
     */
    @GET(ApiConfig.PROJECT_BY_ID_URL)
    Call<ApiResponse<ProjectResponse>> getProjectById(@Path("id") Long id);
    
    /**
     * 删除项目
     * @param id 项目ID
     * @return 删除结果
     */
    @DELETE(ApiConfig.PROJECT_BY_ID_URL)
    Call<ApiResponse<Boolean>> deleteProject(@Path("id") Long id);

    /**
     * 保存沥青原料
     * @param request 沥青原料请求参数
     * @return 沥青原料响应
     */
    @POST(ApiConfig.ASPHALT_MATERIAL_URL)
    Call<ApiResponse<AsphaltMaterialResponse>> createAsphaltMaterial(@Body AsphaltMaterialRequest request);
    
    /**
     * 获取所有沥青原料
     * @param companyId 公司ID
     * @return 沥青原料列表
     */
    @GET(ApiConfig.ASPHALT_MATERIAL_URL)
    Call<ApiResponse<List<AsphaltMaterialResponse>>> getAllAsphaltMaterials(@Query("companyId") String companyId);
    
    /**
     * 保存沙子原料
     * @param request 沙子原料请求参数
     * @return 沙子原料响应
     */
    @POST(ApiConfig.SAND_MATERIAL_URL)
    Call<ApiResponse<SandMaterialResponse>> createSandMaterial(@Body SandMaterialRequest request);
    
    /**
     * 获取所有沙子原料
     * @param companyId 公司ID
     * @return 沙子原料列表
     */
    @GET(ApiConfig.SAND_MATERIAL_URL)
    Call<ApiResponse<List<SandMaterialResponse>>> getAllSandMaterials(@Query("companyId") String companyId);
    
    /**
     * 保存石子原料
     * @param request 石子原料请求参数
     * @return 石子原料响应
     */
    @POST(ApiConfig.STONE_MATERIAL_URL)
    Call<ApiResponse<StoneMaterialResponse>> createStoneMaterial(@Body StoneMaterialRequest request);
    
    /**
     * 获取所有石子原料
     * @param companyId 公司ID
     * @return 石子原料列表
     */
    @GET(ApiConfig.STONE_MATERIAL_URL)
    Call<ApiResponse<List<StoneMaterialResponse>>> getAllStoneMaterials(@Query("companyId") String companyId);

    /**
     * 获取所有混合料任务类型
     * @return 混合料任务类型列表
     */
    @GET(ApiConfig.GET_ALL_MIXTURE_TASKS_URL)
    Call<ApiResponse<List<MixtureTaskModel>>> getAllMixtureTasks();

    /**
     * 根据类型获取混合料任务
     * @param taskType 任务类型
     * @return 混合料任务类型列表
     */
    @GET(ApiConfig.GET_MIXTURE_TASKS_BY_TYPE_URL)
    Call<List<MixtureTaskModel>> getMixtureTasksByType(@Query("taskType") String taskType);

    /**
     * 获取支持的混合料任务类型列表
     * @param taskType 任务类型
     * @return 支持的混合料任务类型列表
     */
    @GET(ApiConfig.GET_SUPPORTED_MIXTURE_TASKS_URL)
    Call<List<SupportMixtureTaskModel>> getSupportedMixtureTasks(@Query("taskType") String taskType);

    /**
     * 保存用户混合料任务
     * @param request 混合料任务请求
     * @return 保存结果响应
     */
    @POST(ApiConfig.SAVE_MIXTURE_TASK_URL)
    Call<ApiResponse<String>> saveMixtureTask(@Body MixtureTaskRequest request);
    
    /**
     * 获取当前用户的混合料任务
     * @return 用户任务列表
     */
    @GET(ApiConfig.GET_USER_MIXTURE_TASKS_URL)
    Call<ApiResponse<List<MixtureTaskModel>>> getUserMixtureTasks();
    
    /**
     * 获取当前用户单位的混合料任务
     * @return 单位任务列表
     */
    @GET(ApiConfig.GET_COMPANY_MIXTURE_TASKS_URL)
    Call<ApiResponse<List<MixtureTaskModel>>> getCompanyMixtureTasks();
}
