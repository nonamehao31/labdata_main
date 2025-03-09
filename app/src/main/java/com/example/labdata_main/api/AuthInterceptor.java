package com.example.labdata_main.api;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.labdata_main.utils.SharedPrefsManager;
import com.example.labdata_main.utils.JwtUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 认证拦截器，用于添加认证令牌到每个请求的头部
 */
public class AuthInterceptor implements Interceptor {
    
    private static final String TAG = "AuthInterceptor";
    private final SharedPrefsManager sharedPrefsManager;
    private final Context appContext;
    
    public AuthInterceptor(SharedPrefsManager sharedPrefsManager, Context appContext) {
        this.sharedPrefsManager = sharedPrefsManager;
        this.appContext = appContext;
    }
    
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 获取请求
        Request request = chain.request();
        Log.d(TAG, "发送请求到: " + request.url());
        
        // 从SharedPreferences中获取认证令牌
        String authToken = sharedPrefsManager.getAuthHeader();
        
        if (authToken != null && !authToken.isEmpty()) {
            Log.d(TAG, "认证状态: 有令牌");
            Log.d(TAG, "已添加认证头: " + authToken);
            
            // 解析并记录JWT令牌的详细信息
            JwtUtils.decodeAndLogJwt(authToken);
            
            // 添加认证头
            request = request.newBuilder()
                    .header("Authorization", authToken)
                    .build();
        } else {
            Log.d(TAG, "认证状态: 无令牌");
        }
        
        // 发送请求并获取响应
        Response response = chain.proceed(request);
        
        // 记录当前时间和时区信息用于调试
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault());
        Date now = new Date();
        Log.d(TAG, "当前设备时间: " + sdf.format(now) + " (" + now.getTime() + ")");
        Log.d(TAG, "设备默认时区: " + TimeZone.getDefault().getID() + ", 偏移: " + TimeZone.getDefault().getRawOffset()/3600000 + "小时");
        
        // 记录响应状态码
        int statusCode = response.code();
        Log.d(TAG, "响应状态码: " + statusCode);
        
        // 如果状态码是401（未授权），可能是令牌已过期
        if (response.code() == 401) {
            Log.e(TAG, "认证失败: 令牌可能过期或无效");
            Intent tokenExpiredIntent = new Intent("com.example.labdata_main.TOKEN_EXPIRED");
            appContext.sendBroadcast(tokenExpiredIntent);
            Log.d(TAG, "已发送令牌过期广播");
        }
        
        return response;
    }
}
