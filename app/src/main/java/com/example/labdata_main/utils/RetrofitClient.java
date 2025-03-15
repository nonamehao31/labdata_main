package com.example.labdata_main.utils;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * Retrofit工具类，用于创建和配置Retrofit实例
 */
public class RetrofitClient {
    // API基础URL
    private static final String BASE_URL = "http://10.11.232.216:8080/";
    
    private static Retrofit retrofit = null;
    
    /**
     * 获取Retrofit客户端实例
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // 创建OkHttpClient并配置
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS);
            
            // 添加日志拦截器用于调试
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(loggingInterceptor);
            
            // 构建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 重置Retrofit客户端实例
     */
    public static void resetClient() {
        retrofit = null;
    }
}
