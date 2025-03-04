package com.example.labdata_main.api;

import com.example.labdata_main.api.request.LoginRequest;
import com.example.labdata_main.api.request.RegisterRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

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
    Call<ApiResponse> register(@Body RegisterRequest request);
}
