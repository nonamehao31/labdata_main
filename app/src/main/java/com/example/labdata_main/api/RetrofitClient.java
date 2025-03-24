package com.example.labdata_main.api;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit客户端管理类，用于创建API服务实例
 */
public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private Context context;
    
    /**
     * 认证拦截器，用于在每个请求中添加JWT令牌
     */
    private class AuthenticationInterceptor implements Interceptor {
        private final SharedPrefsManager sharedPrefsManager;
        
        public AuthenticationInterceptor(Context context) {
            this.sharedPrefsManager = new SharedPrefsManager(context);
        }
        
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            String authHeader = sharedPrefsManager.getAuthHeader();
            
            if (authHeader != null) {
                Log.d(TAG, "Adding auth header to request: " + originalRequest.url());
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", authHeader)
                        .build();
                return chain.proceed(newRequest);
            }
            
            Log.w(TAG, "No auth token available for request: " + originalRequest.url());
            return chain.proceed(originalRequest);
        }
    }
    
    /**
     * 私有构造函数，创建Retrofit实例
     */
    private RetrofitClient(Context context) {
        this.context = context;
        
        // 创建HTTP日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建认证拦截器
        AuthenticationInterceptor authInterceptor = new AuthenticationInterceptor(context);
        
        // 配置OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor) // 先添加认证拦截器
                .addInterceptor(loggingInterceptor) // 再添加日志拦截器，以便记录完整请求
                .build();
        
        // 创建Gson对象，处理日期格式
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create();
        
        // 创建Retrofit对象
        retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    
    /**
     * 获取RetrofitClient实例（单例模式）
     */
    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }
    
    /**
     * 创建API服务接口
     * @param serviceClass API服务接口类
     * @return API服务实例
     */
    public <T> T createService(Class<T> serviceClass) {
        return retrofit.create(serviceClass);
    }
}
