package com.example.labdata_main.api;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.model.ApiResponse;
import com.example.labdata_main.model.Specimen;
import com.example.labdata_main.model.SpecimenCreateDTO;
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

/**
 * 与后端Specimen API交互的服务类
 */
public class SpecimenApiService {
    private static final String TAG = "SpecimenApiService";
    private static final String BASE_URL = "http://10.11.232.216:8080/api/"; // 使用配置文件中定义的真实 IP 地址
    private final SpecimenApi api;

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
                            for (String name : newRequest.headers().names()) {
                                Log.d(TAG, "AuthInterceptor - 请求头: " + name + ": " + newRequest.header(name));
                            }
                            
                            return chain.proceed(newRequest);
                        }
                    }
                }
                
                Log.w(TAG, "AuthInterceptor - 无法通过任何方式获取认证令牌，发送原始请求");
                
                // 发生异常时，继续发送原始请求
                return chain.proceed(chain.request());
            } catch (Exception e) {
                Log.e(TAG, "AuthInterceptor - 处理认证时发生异常", e);
                
                // 发生异常时，继续发送原始请求
                return chain.proceed(chain.request());
            }
        }
    }

    public interface SpecimenApi {
        @GET("specimens/mixratio/{mixRatioId}")
        Call<ApiResponse<List<Specimen>>> getSpecimensByMixRatioId(@Path("mixRatioId") Long mixRatioId);
        
        @GET("specimens/{id}")
        Call<ApiResponse<Specimen>> getSpecimenById(@Path("id") Long id);
        
        @POST("specimens")
        Call<ApiResponse<Specimen>> createSpecimen(@Body Specimen specimen);
        
        @POST("specimens")
        Call<ApiResponse<Specimen>> createSpecimenWithDTO(@Body SpecimenCreateDTO dto);
        
        @POST("specimens/batch")
        Call<ApiResponse<List<Specimen>>> createSpecimens(@Body List<Specimen> specimens);
        
        @PUT("specimens/{id}")
        Call<ApiResponse<Specimen>> updateSpecimen(@Path("id") Long id, @Body Specimen specimen);
        
        @DELETE("specimens/{id}")
        Call<ApiResponse<Void>> deleteSpecimen(@Path("id") Long id);
    }

    public SpecimenApiService(Context context) {
        // 确保 AuthService 初始化
        if (AuthService.getApplicationContext() == null) {
            Log.d(TAG, "在SpecimenApiService构造函数中初始化AuthService");
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

        Log.d(TAG, "初始化SpecimenApiService，BASE_URL: " + BASE_URL);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SpecimenApi.class);
    }

    // 无参构造函数，保持向后兼容性
    public SpecimenApiService() {
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

        Log.d(TAG, "初始化SpecimenApiService，BASE_URL: " + BASE_URL);
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(SpecimenApi.class);
    }

    /**
     * 获取指定配比ID的所有试件
     * @param mixRatioId 配比ID
     * @param callback 回调函数
     */
    public void getSpecimensByMixRatioId(Long mixRatioId, Callback<ApiResponse<List<Specimen>>> callback) {
        Log.d(TAG, "调用getSpecimensByMixRatioId API, mixRatioId: " + mixRatioId);
        api.getSpecimensByMixRatioId(mixRatioId).enqueue(callback);
    }
    
    /**
     * 根据ID获取试件
     * @param id 试件ID
     * @param callback 回调函数
     */
    public void getSpecimenById(Long id, Callback<ApiResponse<Specimen>> callback) {
        Log.d(TAG, "调用getSpecimenById API, id: " + id);
        api.getSpecimenById(id).enqueue(callback);
    }
    
    /**
     * 创建试件
     * @param specimen 试件对象
     * @param callback 回调函数
     */
    public void createSpecimen(Specimen specimen, Callback<ApiResponse<Specimen>> callback) {
        Log.d(TAG, "调用createSpecimen API, mixRatioId: " + specimen.getMixRatioId());
        api.createSpecimen(specimen).enqueue(callback);
    }
    
    /**
     * 批量创建试件
     * @param specimens 试件列表
     * @param callback 回调函数
     */
    public void createSpecimens(List<Specimen> specimens, Callback<ApiResponse<List<Specimen>>> callback) {
        Log.d(TAG, "调用createSpecimens API, 试件数量: " + specimens.size());
        api.createSpecimens(specimens).enqueue(callback);
    }
    
    /**
     * 更新试件
     * @param id 试件ID
     * @param specimen 试件对象
     * @param callback 回调函数
     */
    public void updateSpecimen(Long id, Specimen specimen, Callback<ApiResponse<Specimen>> callback) {
        Log.d(TAG, "调用updateSpecimen API, id: " + id);
        api.updateSpecimen(id, specimen).enqueue(callback);
    }
    
    /**
     * 删除试件
     * @param id 试件ID
     * @param callback 回调函数
     */
    public void deleteSpecimen(Long id, Callback<ApiResponse<Void>> callback) {
        Log.d(TAG, "调用deleteSpecimen API, id: " + id);
        api.deleteSpecimen(id).enqueue(callback);
    }
    
    /**
     * 创建试件（使用DTO避免发送ID字段）
     * @param specimen 源试件对象
     * @param callback 回调函数
     */
    public void createSpecimenWithDTO(Specimen specimen, Callback<ApiResponse<Specimen>> callback) {
        SpecimenCreateDTO dto = SpecimenCreateDTO.fromSpecimen(specimen);
        Log.d(TAG, "调用createSpecimenWithDTO API, mixRatioId: " + dto.getMixRatioId());
        api.createSpecimenWithDTO(dto).enqueue(callback);
    }
}
