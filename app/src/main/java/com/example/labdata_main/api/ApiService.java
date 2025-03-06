package com.example.labdata_main.api;

import com.example.labdata_main.api.request.DeviceRequest;
import com.example.labdata_main.api.request.LoginRequest;
import com.example.labdata_main.api.request.RegisterRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.api.response.LoginResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
}
