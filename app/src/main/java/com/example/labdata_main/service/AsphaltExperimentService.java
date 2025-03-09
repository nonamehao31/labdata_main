package com.example.labdata_main.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiService;
import com.example.labdata_main.api.dto.AsphaltExperimentRequest;
import com.example.labdata_main.api.dto.AsphaltExperimentResponse;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.model.AsphaltInfo;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.model.ExperimentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 沥青实验服务类，处理沥青实验相关的API请求
 */
public class AsphaltExperimentService {
    private static final String TAG = "AsphaltExperimentSvc";
    private final Context context;
    private final ApiService apiService;

    public AsphaltExperimentService(Context context) {
        this.context = context;
        this.apiService = ApiClient.getInstance();
    }

    /**
     * 创建沥青实验任务
     * @param taskName 任务名称
     * @param description 任务描述
     * @param selectedAsphalt 选中的沥青信息列表
     * @param experimentAssignments 实验分配信息（沥青与实验类型的关系）
     * @param callback 回调函数，返回创建的任务ID
     */
    public void createAsphaltExperimentTask(
            String taskName,
            String description,
            Set<AsphaltInfo> selectedAsphalt,
            List<ExperimentAssignment> experimentAssignments,
            final ServiceCallback<Long> callback) {
        
        // 创建请求对象
        AsphaltExperimentRequest request = new AsphaltExperimentRequest();
        request.setTaskName(taskName);
        request.setDescription(description);
        request.setClientId(generateTemporaryId()); // 生成临时客户端ID
        
        // 处理沥青数据
        List<AsphaltExperimentRequest.AsphaltData> asphaltDataList = new ArrayList<>();
        AtomicLong tempId = new AtomicLong(System.currentTimeMillis());
        
        for (AsphaltInfo asphalt : selectedAsphalt) {
            AsphaltExperimentRequest.AsphaltData asphaltData = new AsphaltExperimentRequest.AsphaltData();
            
            // 设置沥青基本信息
            Long asphaltClientId = tempId.incrementAndGet();
            asphaltData.setClientId(asphaltClientId);
            asphaltData.setGrade(asphalt.getGrade());
            asphaltData.setType(asphalt.getType());
            asphaltData.setSupplier(asphalt.getSupplier());
            asphaltData.setExpiryDate(asphalt.getExpiryDate());
            
            // 处理实验分配
            List<Long> experimentTypeIds = new ArrayList<>();
            List<Long> assignmentClientIds = new ArrayList<>();
            
            for (ExperimentAssignment assignment : experimentAssignments) {
                if (assignment.getAsphaltInfo().equals(asphalt)) {
                    experimentTypeIds.add(assignment.getExperimentType().getId());
                    assignmentClientIds.add(tempId.incrementAndGet());
                }
            }
            
            asphaltData.setExperimentTypeIds(experimentTypeIds);
            asphaltData.setAssignmentClientIds(assignmentClientIds);
            
            asphaltDataList.add(asphaltData);
        }
        
        request.setAsphaltDataList(asphaltDataList);
        
        // 发送API请求
        apiService.createAsphaltExperimentTask(request).enqueue(new Callback<ApiResponse<Long>>() {
            @Override
            public void onResponse(Call<ApiResponse<Long>> call, Response<ApiResponse<Long>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "沥青实验任务创建成功: " + response.body().getData());
                    callback.onSuccess(response.body().getData());
                } else {
                    String errorMsg = response.isSuccessful() ? 
                            response.body().getMessage() : "服务器错误: " + response.code();
                    Log.e(TAG, "沥青实验任务创建失败: " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Long>> call, Throwable t) {
                Log.e(TAG, "沥青实验任务创建请求失败", t);
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }
    
    /**
     * 获取沥青实验任务详情
     * @param taskId 任务ID
     * @param callback 回调函数
     */
    public void getAsphaltExperimentTask(Long taskId, final ServiceCallback<AsphaltExperimentResponse> callback) {
        apiService.getAsphaltExperimentTaskById(taskId).enqueue(new Callback<ApiResponse<AsphaltExperimentResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AsphaltExperimentResponse>> call, 
                                  Response<ApiResponse<AsphaltExperimentResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    String errorMsg = response.isSuccessful() ? 
                            response.body().getMessage() : "服务器错误: " + response.code();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AsphaltExperimentResponse>> call, Throwable t) {
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }
    
    /**
     * 获取所有沥青实验任务
     * @param callback 回调函数
     */
    public void getAllAsphaltExperimentTasks(final ServiceCallback<List<ExperimentTask>> callback) {
        apiService.getAsphaltExperimentTasks().enqueue(new Callback<ApiResponse<List<ExperimentTask>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ExperimentTask>>> call, 
                                  Response<ApiResponse<List<ExperimentTask>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(response.body().getData());
                } else {
                    String errorMsg = response.isSuccessful() ? 
                            response.body().getMessage() : "服务器错误: " + response.code();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ExperimentTask>>> call, Throwable t) {
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }
    
    /**
     * 更新沥青实验任务
     * @param taskId 任务ID
     * @param request 更新请求
     * @param callback 回调函数
     */
    public void updateAsphaltExperimentTask(Long taskId, AsphaltExperimentRequest request, 
                                           final ServiceCallback<Void> callback) {
        apiService.updateAsphaltExperimentTask(taskId, request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = response.isSuccessful() ? 
                            response.body().getMessage() : "服务器错误: " + response.code();
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                callback.onError("网络请求失败: " + t.getMessage());
            }
        });
    }
    
    /**
     * 实验分配信息（临时类，用于传递沥青与实验类型的关系）
     */
    public static class ExperimentAssignment {
        private AsphaltInfo asphaltInfo;
        private ExperimentType experimentType;
        
        public ExperimentAssignment(AsphaltInfo asphaltInfo, ExperimentType experimentType) {
            this.asphaltInfo = asphaltInfo;
            this.experimentType = experimentType;
        }
        
        public AsphaltInfo getAsphaltInfo() {
            return asphaltInfo;
        }
        
        public ExperimentType getExperimentType() {
            return experimentType;
        }
    }
    
    /**
     * 回调接口
     * @param <T> 返回数据类型
     */
    public interface ServiceCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    /**
     * 生成临时客户端ID
     */
    private long generateTemporaryId() {
        return System.currentTimeMillis() + (long) (Math.random() * 10000);
    }
}
