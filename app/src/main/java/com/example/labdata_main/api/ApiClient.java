package com.example.labdata_main.api;

import android.content.Context;

import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API客户端配置类，使用Retrofit和OkHttp创建API服务
 */
public class ApiClient {
    private static Retrofit retrofit = null;
    private static Context appContext = null;
    
    /**
     * 初始化ApiClient的应用上下文
     * @param context 应用上下文
     */
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }
    
    /**
     * 重置Retrofit客户端实例
     * 在登录状态改变后调用此方法以确保新的认证令牌被使用
     */
    public static void resetClient() {
        retrofit = null;
    }
    
    public static Retrofit getClient() {
        if (retrofit == null) {
            if (appContext == null) {
                throw new IllegalStateException("ApiClient未初始化，请先调用init方法");
            }
            
            // 创建OkHttp日志拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // 创建认证拦截器
            SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(appContext);
            AuthInterceptor authInterceptor = new AuthInterceptor(sharedPrefsManager);
            
            // 创建OkHttp客户端
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor) // 添加认证拦截器
                    .addInterceptor(loggingInterceptor)
                    .build();
            
            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 获取API服务实例
     * @return ApiService实例
     */
    public static ApiService getApiService() {
        return getClient().create(ApiService.class);
    }
}
