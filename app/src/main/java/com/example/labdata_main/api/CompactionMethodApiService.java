package com.example.labdata_main.api;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MoldingMethod;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 与后端CompactionMethod API交互的服务类
 */
public class CompactionMethodApiService {
    private static final String TAG = "CompactionMethodApiService";
    private final CompactionMethodService api;

    /**
     * 构造函数，需要提供Context用于认证
     * @param context 应用上下文
     */
    public CompactionMethodApiService(Context context) {
        // 确保 AuthService 初始化
        if (AuthService.getApplicationContext() == null) {
            Log.d(TAG, "在CompactionMethodApiService构造函数中初始化AuthService");
            AuthService.init(context);
        }
        
        // 添加HTTP请求日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> 
            Log.d(TAG, "HTTP请求日志: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建SharedPrefsManager实例
        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(context);
        
        // 创建包含认证拦截器的OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthInterceptor(sharedPrefsManager, context))
                .build();
        
        // 创建Retrofit实例并初始化API接口
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
                
        api = retrofit.create(CompactionMethodService.class);
    }
    
    /**
     * 获取当前用户所属单位的制件方法列表
     * @param callback 回调函数
     */
    public void getOrganizationCompactionMethods(Callback<ApiResponse<List<MoldingMethod>>> callback) {
        Log.d(TAG, "调用API: 获取当前用户所属单位的制件方法列表");
        Call<ApiResponse<List<MoldingMethod>>> call = api.getOrganizationCompactionMethods();
        call.enqueue(new Callback<ApiResponse<List<MoldingMethod>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MoldingMethod>>> call, Response<ApiResponse<List<MoldingMethod>>> response) {
                // 处理响应并传递给原始回调
                callback.onResponse(call, response);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<MoldingMethod>>> call, Throwable t) {
                // 转发失败信息
                callback.onFailure(call, t);
            }
        });
    }
    
    /**
     * 根据单位ID获取制件方法列表
     * @param organizationId 单位ID
     * @param callback 回调函数
     */
    public void getCompactionMethodsByOrganization(Long organizationId, Callback<ApiResponse<List<MoldingMethod>>> callback) {
        Log.d(TAG, "调用API: 根据单位ID获取制件方法列表, 单位ID: " + organizationId);
        Call<ApiResponse<List<MoldingMethod>>> call = api.getCompactionMethodsByOrganization(organizationId);
        call.enqueue(new Callback<ApiResponse<List<MoldingMethod>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MoldingMethod>>> call, Response<ApiResponse<List<MoldingMethod>>> response) {
                // 处理响应并传递给原始回调
                callback.onResponse(call, response);
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<MoldingMethod>>> call, Throwable t) {
                // 转发失败信息
                callback.onFailure(call, t);
            }
        });
    }
}
