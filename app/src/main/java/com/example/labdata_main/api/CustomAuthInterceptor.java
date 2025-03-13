package com.example.labdata_main.api;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 自定义认证拦截器，用于设置请求头中的认证令牌
 */
public class CustomAuthInterceptor implements Interceptor {
    private final String token;

    /**
     * 构造函数
     * @param token 认证令牌
     */
    public CustomAuthInterceptor(String token) {
        this.token = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // 检查token格式，避免重复的Bearer前缀
        String authHeader = token;
        // 如果token已经包含Bearer前缀，直接使用，否则添加Bearer前缀
        if (!token.startsWith("Bearer ")) {
            authHeader = "Bearer " + token;
        }

        // 添加认证头
        Request.Builder requestBuilder = original.newBuilder()
                .header("Authorization", authHeader)
                .method(original.method(), original.body());

        Request request = requestBuilder.build();
        return chain.proceed(request);
    }
}
