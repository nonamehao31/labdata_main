package com.example.labdata_main.api;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.MixRatioRequest;
import com.example.labdata_main.model.MixRatioResponse;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class MixRatioApiService {
    private static final String TAG = "MixRatioApiService";
    private static final String BASE_URL = "http://10.11.232.216:8080/api/"; // 使用配置文件中定义的真实 IP 地址
    private final MixRatioApi api;

    /**
     * 认证拦截器，为每个请求动态添加JWT令牌
     */
    private static class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            try {
                // 从AuthService获取JWT令牌
                String token = AuthService.getJwtToken();
                
                Log.d(TAG, "AuthInterceptor - 获取的JWT令牌: " + (token != null ? "有效令牌" : "无效令牌"));
                
                Request originalRequest = chain.request();
                Log.d(TAG, "AuthInterceptor - 原始请求URL: " + originalRequest.url());
                Log.d(TAG, "AuthInterceptor - 原始请求方法: " + originalRequest.method());
                
                // 如果获取到了令牌，则添加到请求头中
                if (token != null && !token.isEmpty()) {
                    String authHeader = "Bearer " + token;
                    Log.d(TAG, "AuthInterceptor - 添加认证头: " + authHeader);
                    
                    // 创建新的请求，添加认证头
                    Request.Builder builder = originalRequest.newBuilder()
                            .header("Authorization", authHeader)  // 添加认证头
                            .header("Content-Type", "application/json")  // 确保内容类型正确
                            .method(originalRequest.method(), originalRequest.body());  // 保持原有的请求方法和请求体
                    
                    Request newRequest = builder.build();
                    
                    Log.d(TAG, "AuthInterceptor - 已添加认证头的请求: " + newRequest.url());
                    for (String name : newRequest.headers().names()) {
                        Log.d(TAG, "AuthInterceptor - 请求头: " + name + ": " + newRequest.header(name));
                    }
                    
                    // 发送带有认证头的请求
                    return chain.proceed(newRequest);
                } else {
                    Log.e(TAG, "AuthInterceptor - 无法获取JWT令牌，发送未认证请求");
                    
                    // 尝试从 SharedPrefsManager 直接获取认证头
                    if (AuthService.getApplicationContext() != null) {
                        SharedPrefsManager sharedPrefsManager = new SharedPrefsManager(AuthService.getApplicationContext());
                        String authHeader = sharedPrefsManager.getAuthHeader();
                        
                        if (authHeader != null && !authHeader.isEmpty()) {
                            Log.d(TAG, "AuthInterceptor - 从 SharedPrefsManager 直接获取认证头: " + authHeader);
                            
                            Request.Builder builder = originalRequest.newBuilder()
                                    .header("Authorization", authHeader)
                                    .header("Content-Type", "application/json");
                            
                            Request newRequest = builder.build();
                            
                            Log.d(TAG, "AuthInterceptor - 使用备选方法添加认证头的请求: " + newRequest.url());
                            return chain.proceed(newRequest);
                        }
                    }
                }
                
                // 如果没有令牌，则继续发送原始请求
                // 这可能会导致401未授权错误，但应用程序会处理这种情况
                Log.w(TAG, "AuthInterceptor - 没有有效的认证令牌，将发送未认证请求");
                return chain.proceed(originalRequest);
            } catch (Exception e) {
                Log.e(TAG, "AuthInterceptor - 添加认证令牌时发生异常: " + e.getMessage(), e);
                // 发生异常时，继续发送原始请求
                return chain.proceed(chain.request());
            }
        }
    }

    public interface MixRatioApi {
        @GET("mixratios")
        Call<ApiResponse<List<MixRatioResponse>>> getAllMixRatios();
        
        @GET("mixratios/{id}")
        Call<ApiResponse<MixRatioResponse>> getMixRatioById(@Path("id") Long id);
        
        @POST("mixratios")
        Call<ApiResponse<MixRatioResponse>> createMixRatio(@Body MixRatioRequest request);
        
        @PUT("mixratios/{id}")
        Call<ApiResponse<MixRatioResponse>> updateMixRatio(@Path("id") Long id, @Body MixRatioRequest request);
        
        @DELETE("mixratios/{id}")
        Call<ApiResponse<Void>> deleteMixRatio(@Path("id") Long id);
        
        @GET("mixratios/comprehensive")
        Call<ApiResponse<List<MixRatioResponse>>> getComprehensiveMixRatios();
    }

    public MixRatioApiService(Context context) {
        // 确保 AuthService 初始化
        if (AuthService.getApplicationContext() == null) {
            Log.d(TAG, "在MixRatioApiService构造函数中初始化AuthService");
            AuthService.init(context);
        }
        
        // 添加HTTP请求日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, "HTTP请求日志: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建包含认证拦截器的OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthInterceptor())
                .build();

        Log.d(TAG, "初始化MixRatioApiService，BASE_URL: " + BASE_URL);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(MixRatioApi.class);
    }

    // 无参构造函数，保持向后兼容性
    public MixRatioApiService() {
        // 添加HTTP请求日志拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> Log.d(TAG, "HTTP请求日志: " + message));
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        // 创建包含认证拦截器的OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(new AuthInterceptor())
                .build();

        Log.d(TAG, "初始化MixRatioApiService，BASE_URL: " + BASE_URL);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(MixRatioApi.class);
    }

    public void getAllMixRatios(Callback<ApiResponse<List<MixRatioResponse>>> callback) {
        Log.d(TAG, "调用getAllMixRatios API");
        api.getAllMixRatios().enqueue(callback);
    }
    
    public void getMixRatioById(Long id, Callback<ApiResponse<MixRatioResponse>> callback) {
        Log.d(TAG, "调用getMixRatioById API, id: " + id);
        api.getMixRatioById(id).enqueue(callback);
    }
    
    public void createMixRatio(MixRatioRequest request, Callback<ApiResponse<MixRatioResponse>> callback) {
        Log.d(TAG, "调用createMixRatio API, 配比名称: " + request.getMixName());
        api.createMixRatio(request).enqueue(callback);
    }
    
    public void updateMixRatio(Long id, MixRatioRequest request, Callback<ApiResponse<MixRatioResponse>> callback) {
        Log.d(TAG, "调用updateMixRatio API, id: " + id + ", 配比名称: " + request.getMixName());
        api.updateMixRatio(id, request).enqueue(callback);
    }
    
    public void deleteMixRatio(Long id, Callback<ApiResponse<Void>> callback) {
        Log.d(TAG, "调用deleteMixRatio API, id: " + id);
        api.deleteMixRatio(id).enqueue(callback);
    }
    
    public void getComprehensiveMixRatios(Callback<ApiResponse<List<MixRatioResponse>>> callback) {
        Log.d(TAG, "调用getComprehensiveMixRatios API");
        api.getComprehensiveMixRatios().enqueue(callback);
    }
}
