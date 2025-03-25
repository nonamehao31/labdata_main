package com.example.labdata_main.api;

import android.content.Context;

import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.DeviceResponse;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * API客户端配置类，使用Retrofit和OkHttp创建API服务
 */
public class ApiClient {
    private static Retrofit retrofit = null;
    private static Context appContext = null;
    private static ApiService apiService = null;
    private static final String TAG = "ApiClient";
    
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
        apiService = null;
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
            AuthInterceptor authInterceptor = new AuthInterceptor(sharedPrefsManager, appContext);
            
            // 创建OkHttp客户端
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(authInterceptor) // 添加认证拦截器
                    .addInterceptor(loggingInterceptor)
                    .build();
            
            // 配置Gson，使其能正确处理ISO 8601格式的日期字符串
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                    .create();
            
            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(ApiConfig.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
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
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }
    
    /**
     * 获取单例API客户端实例
     * @return ApiService实例
     */
    public static ApiService getInstance() {
        return getApiService();
    }
    
    /**
     * 使用自定义Token创建API服务实例
     * @param serviceClass 服务类
     * @param token 认证令牌
     * @param <T> 服务类型
     * @return 服务实例
     */
    public static <T> T createService(Class<T> serviceClass, String token) {
        if (appContext == null) {
            throw new IllegalStateException("ApiClient未初始化，请先调用init方法");
        }
        
        // 创建OkHttp日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建自定义Token的认证拦截器
        CustomAuthInterceptor authInterceptor = new CustomAuthInterceptor(token);
        
        // 创建OkHttp客户端
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(ApiConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(ApiConfig.READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(ApiConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor) // 添加认证拦截器
                .addInterceptor(loggingInterceptor)
                .build();
        
        // 配置Gson，使其能正确处理ISO 8601格式的日期字符串
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                .create();
        
        // 创建Retrofit实例
        Retrofit customRetrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
        
        return customRetrofit.create(serviceClass);
    }
    
    /**
     * 检查公司设备是否已初始化
     * 如果公司ID为空或无效，将抛出异常
     * @param companyId 公司ID
     * @return 请求对象
     */
    public Call<ApiResponse<Boolean>> checkCompanyEquipmentInitialized(String companyId) {
        if (companyId == null || companyId.trim().isEmpty()) {
            throw new IllegalArgumentException("检查设备初始化状态需要有效的公司ID");
        }
        return getApiService().checkCompanyEquipmentInitialized(companyId);
    }
    
    /**
     * 获取公司设备列表
     * 如果公司ID为空或无效，将抛出异常
     * @param companyId 公司ID
     * @return 请求对象
     */
    public Call<ApiResponse<List<DeviceResponse>>> getCompanyEquipment(String companyId) {
        if (companyId == null || companyId.trim().isEmpty()) {
            throw new IllegalArgumentException("获取公司设备列表需要有效的公司ID");
        }
        return getApiService().getCompanyEquipment(companyId);
    }
}
