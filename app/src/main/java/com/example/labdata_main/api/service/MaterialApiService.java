package com.example.labdata_main.api.service;

import android.util.Log;
import java.io.IOException;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiConfig;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.request.AsphaltMaterialRequest;
import com.example.labdata_main.api.request.SandMaterialRequest;
import com.example.labdata_main.api.request.StoneMaterialRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.api.response.AsphaltMaterialResponse;
import com.example.labdata_main.api.response.SandMaterialResponse;
import com.example.labdata_main.api.response.StoneMaterialResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 材料API服务类，处理与材料相关的API调用
 */
public class MaterialApiService {
    private static final String TAG = "MaterialApiService";
    private final ApiService apiService;

    public MaterialApiService() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    /**
     * 保存沥青原料
     * @param name 名称
     * @param grade 沥青标号
     * @param character 针入度
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void saveAsphaltMaterial(String name, String grade, String character, String companyId,
                                   final ApiCallback<AsphaltMaterialResponse> callback) {
        AsphaltMaterialRequest request = new AsphaltMaterialRequest(name, grade, character, companyId);
        Call<ApiResponse<AsphaltMaterialResponse>> call = apiService.createAsphaltMaterial(request);
        
        // 打印请求信息用于调试
        Log.d(TAG, "发送沥青材料API请求: endpoint=" + ApiConfig.ASPHALT_MATERIAL_URL 
                + ", baseUrl=" + ApiConfig.BASE_URL 
                + ", 请求体=" + request.getName() + "," + request.getGrade() 
                + "," + request.getCharacter() + ", companyId=" + request.getCompanyId());
        
        call.enqueue(new Callback<ApiResponse<AsphaltMaterialResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AsphaltMaterialResponse>> call, 
                                  Response<ApiResponse<AsphaltMaterialResponse>> response) {
                Log.d(TAG, "收到沥青材料API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "保存沥青原料失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "沥青材料API调用成功: " + (response.body().getData() != null ? 
                        "id=" + response.body().getData().getId() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<AsphaltMaterialResponse>> call, Throwable t) {
                Log.e(TAG, "保存沥青原料网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 保存沙子原料
     * @param name 名称
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void saveSandMaterial(String name, String companyId, final ApiCallback<SandMaterialResponse> callback) {
        SandMaterialRequest request = new SandMaterialRequest(name, companyId);
        Call<ApiResponse<SandMaterialResponse>> call = apiService.createSandMaterial(request);
        
        // 打印请求信息用于调试
        Log.d(TAG, "发送沙子材料API请求: endpoint=" + ApiConfig.SAND_MATERIAL_URL 
                + ", baseUrl=" + ApiConfig.BASE_URL 
                + ", 请求体=" + request.getName() + ", companyId=" + request.getCompanyId());
        
        call.enqueue(new Callback<ApiResponse<SandMaterialResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<SandMaterialResponse>> call, 
                                  Response<ApiResponse<SandMaterialResponse>> response) {
                Log.d(TAG, "收到沙子材料API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "保存沙子原料失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "沙子材料API调用成功: " + (response.body().getData() != null ? 
                        "id=" + response.body().getData().getId() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<SandMaterialResponse>> call, Throwable t) {
                Log.e(TAG, "保存沙子原料网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 保存沙子原料 (兼容旧方法)
     * @param name 名称
     * @param callback 回调
     */
    public void saveSandMaterial(String name, final ApiCallback<SandMaterialResponse> callback) {
        saveSandMaterial(name, "unknown", callback);
    }

    /**
     * 保存石子原料
     * @param name 名称
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void saveStoneMaterial(String name, String companyId, final ApiCallback<StoneMaterialResponse> callback) {
        StoneMaterialRequest request = new StoneMaterialRequest(name, companyId);
        Call<ApiResponse<StoneMaterialResponse>> call = apiService.createStoneMaterial(request);
        
        // 打印请求信息用于调试
        Log.d(TAG, "发送石子材料API请求: endpoint=" + ApiConfig.STONE_MATERIAL_URL 
                + ", baseUrl=" + ApiConfig.BASE_URL 
                + ", 请求体=" + request.getName() + ", companyId=" + request.getCompanyId());
        
        call.enqueue(new Callback<ApiResponse<StoneMaterialResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<StoneMaterialResponse>> call, 
                                  Response<ApiResponse<StoneMaterialResponse>> response) {
                Log.d(TAG, "收到石子材料API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "保存石子原料失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "石子材料API调用成功: " + (response.body().getData() != null ? 
                        "id=" + response.body().getData().getId() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<StoneMaterialResponse>> call, Throwable t) {
                Log.e(TAG, "保存石子原料网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 保存石子原料 (兼容旧方法)
     * @param name 名称
     * @param callback 回调
     */
    public void saveStoneMaterial(String name, final ApiCallback<StoneMaterialResponse> callback) {
        saveStoneMaterial(name, "unknown", callback);
    }

    /**
     * 获取所有沥青原料
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void getAllAsphaltMaterials(String companyId, final ApiCallback<List<AsphaltMaterialResponse>> callback) {
        Call<ApiResponse<List<AsphaltMaterialResponse>>> call = apiService.getAllAsphaltMaterials(companyId);
        
        call.enqueue(new Callback<ApiResponse<List<AsphaltMaterialResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AsphaltMaterialResponse>>> call, 
                                  Response<ApiResponse<List<AsphaltMaterialResponse>>> response) {
                Log.d(TAG, "收到沥青材料列表API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "获取沥青原料列表失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "沥青材料列表API调用成功: " + (response.body().getData() != null ? 
                        "列表大小=" + response.body().getData().size() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AsphaltMaterialResponse>>> call, Throwable t) {
                Log.e(TAG, "获取沥青原料列表网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取所有沥青原料 (兼容旧方法)
     * @param callback 回调
     */
    public void getAllAsphaltMaterials(final ApiCallback<List<AsphaltMaterialResponse>> callback) {
        getAllAsphaltMaterials("unknown", callback);
    }

    /**
     * 获取所有沙子原料
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void getAllSandMaterials(String companyId, final ApiCallback<List<SandMaterialResponse>> callback) {
        Call<ApiResponse<List<SandMaterialResponse>>> call = apiService.getAllSandMaterials(companyId);
        
        call.enqueue(new Callback<ApiResponse<List<SandMaterialResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<SandMaterialResponse>>> call, 
                                  Response<ApiResponse<List<SandMaterialResponse>>> response) {
                Log.d(TAG, "收到沙子材料列表API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "获取沙子原料列表失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "沙子材料列表API调用成功: " + (response.body().getData() != null ? 
                        "列表大小=" + response.body().getData().size() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<SandMaterialResponse>>> call, Throwable t) {
                Log.e(TAG, "获取沙子原料列表网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取所有沙子原料 (兼容旧方法)
     * @param callback 回调
     */
    public void getAllSandMaterials(final ApiCallback<List<SandMaterialResponse>> callback) {
        getAllSandMaterials("unknown", callback);
    }

    /**
     * 获取所有石子原料
     * @param companyId 公司ID
     * @param callback 回调
     */
    public void getAllStoneMaterials(String companyId, final ApiCallback<List<StoneMaterialResponse>> callback) {
        Call<ApiResponse<List<StoneMaterialResponse>>> call = apiService.getAllStoneMaterials(companyId);
        
        call.enqueue(new Callback<ApiResponse<List<StoneMaterialResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<StoneMaterialResponse>>> call, 
                                  Response<ApiResponse<List<StoneMaterialResponse>>> response) {
                Log.d(TAG, "收到石子材料列表API响应: code=" + response.code());
                
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "API错误响应: " + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "无法读取错误响应", e);
                    }
                    callback.onFailure("API请求失败，HTTP状态码: " + response.code());
                    return;
                }
                
                if (response.body() == null) {
                    callback.onFailure("API响应体为空");
                    return;
                }
                
                if (!response.body().isSuccess()) {
                    String errorMessage = "获取石子原料列表失败";
                    if (response.body() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Log.e(TAG, "API业务错误: " + errorMessage);
                    callback.onFailure(errorMessage);
                    return;
                }
                
                Log.d(TAG, "石子材料列表API调用成功: " + (response.body().getData() != null ? 
                        "列表大小=" + response.body().getData().size() : "数据为空"));
                callback.onSuccess(response.body().getData());
            }

            @Override
            public void onFailure(Call<ApiResponse<List<StoneMaterialResponse>>> call, Throwable t) {
                Log.e(TAG, "获取石子原料列表网络请求失败", t);
                callback.onFailure("网络请求失败: " + t.getMessage());
            }
        });
    }

    /**
     * 获取所有石子原料 (兼容旧方法)
     * @param callback 回调
     */
    public void getAllStoneMaterials(final ApiCallback<List<StoneMaterialResponse>> callback) {
        getAllStoneMaterials("unknown", callback);
    }

    /**
     * API回调接口
     * @param <T> 响应数据类型
     */
    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onFailure(String errorMessage);
    }
}
