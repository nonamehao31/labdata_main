package com.example.labdata_main.api;

import android.util.Log;

import com.example.labdata_main.utils.SharedPrefsManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 认证拦截器，用于添加认证令牌到每个请求的头部
 */
public class AuthInterceptor implements Interceptor {
    
    private static final String TAG = "AuthInterceptor";
    private final SharedPrefsManager sharedPrefsManager;
    
    public AuthInterceptor(SharedPrefsManager sharedPrefsManager) {
        this.sharedPrefsManager = sharedPrefsManager;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        
        // 获取授权头信息
        String authHeader = sharedPrefsManager.getAuthHeader();
        
        // 日志记录请求URL、认证状态
        String url = originalRequest.url().toString();
        Log.d(TAG, "发送请求到: " + url);
        Log.d(TAG, "认证状态: " + (authHeader != null ? "有令牌" : "无令牌"));
        
        // 如果授权头存在，添加到请求中
        if (authHeader != null) {
            Request.Builder builder = originalRequest.newBuilder()
                    .header("Authorization", authHeader);
            
            Request newRequest = builder.build();
            
            // 日志记录新请求的头部信息
            Log.d(TAG, "已添加认证头: " + authHeader.substring(0, Math.min(10, authHeader.length())) + "...");
            
            Response response = chain.proceed(newRequest);
            
            // 日志记录响应状态码
            Log.d(TAG, "响应状态码: " + response.code());
            
            if (response.code() == 401) {
                Log.e(TAG, "认证失败: 令牌可能过期或无效");
            }
            
            return response;
        }
        
        // 如果没有授权头，直接发送原始请求
        Log.d(TAG, "无认证令牌，发送原始请求");
        return chain.proceed(originalRequest);
    }
}
