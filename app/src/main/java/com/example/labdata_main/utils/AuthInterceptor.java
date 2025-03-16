package com.example.labdata_main.utils;

import android.content.Context;
import android.util.Log;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * 认证拦截器
 * 为所有API请求添加JWT身份验证令牌
 */
public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private final SharedPrefsManager prefsManager;

    public AuthInterceptor(Context context) {
        this.prefsManager = new SharedPrefsManager(context);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // 获取认证令牌
        String authHeader = prefsManager.getAuthHeader();
        
        // 记录请求详情，帮助调试
        Log.d(TAG, "发起请求: " + original.url());
        Log.d(TAG, "请求方法: " + original.method());
        Log.d(TAG, "认证头: " + (authHeader != null ? "已存在" : "不存在"));
        
        // [临时调试] 输出完整的认证令牌和请求信息
        if (authHeader != null) {
            Log.d(TAG, "完整认证令牌: " + authHeader);
        }
        Log.d(TAG, "请求头: " + original.headers().toString());
        
        // 如果没有认证令牌，直接传递原始请求并记录警告
        if (authHeader == null || authHeader.isEmpty()) {
            Log.w(TAG, "警告: 未找到认证令牌，发送未认证请求: " + original.url());
            return chain.proceed(original);
        }
        
        // 添加认证头
        Log.d(TAG, "添加认证头到请求: " + original.url());
        Request.Builder requestBuilder = original.newBuilder()
                .header("Authorization", authHeader)
                .method(original.method(), original.body());
        
        Request modifiedRequest = requestBuilder.build();
        
        // 验证认证头是否已添加
        String requestAuthHeader = modifiedRequest.header("Authorization");
        Log.d(TAG, "验证认证头: " + (requestAuthHeader != null ? "已添加" : "未添加"));
        if (!authHeader.equals(requestAuthHeader)) {
            Log.e(TAG, "错误: 认证头添加失败。请求中的认证头: " + requestAuthHeader);
        }
        
        // [临时调试] 输出修改后的请求头
        Log.d(TAG, "修改后的请求头: " + modifiedRequest.headers().toString());
        
        return chain.proceed(modifiedRequest);
    }
}
