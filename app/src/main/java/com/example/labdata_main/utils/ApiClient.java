package com.example.labdata_main.utils;

import android.content.Context;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * API客户端工具类，用于创建和配置Retrofit实例
 */
public class ApiClient {
    // API基础URL - 使用实际服务器地址
    //public static final String BASE_URL = "http://182.92.76.162:80/";
    public static final String BASE_URL = "http://182.92.76.162:80/api/";
    //public static final String BASE_URL = "http://192.168.42.78:8080/";
    //private static final String BASE_URL = "http://192.168.1.3:8080/";
    private static final String TAG = "ApiClient";
    
    private static Retrofit retrofit = null;
    private static Context appContext = null;
    
    /**
     * 获取API基础URL，用于日志调试
     * @return API基础URL
     */
    public static String getBaseUrl() {
        return BASE_URL;
    }
    
    /**
     * 初始化应用上下文，用于获取SharedPreferences
     * @param context 应用上下文
     */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
        // 重置客户端以确保配置更新生效
        resetClient();
    }
    
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
            
            // 添加认证拦截器
            if (appContext != null) {
                httpClient.addInterceptor(new AuthInterceptor(appContext));
            }
            
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
