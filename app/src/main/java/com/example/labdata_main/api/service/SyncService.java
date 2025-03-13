package com.example.labdata_main.api.service;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.LabDataApplication;
import com.example.labdata_main.api.ApiClient;
import com.example.labdata_main.api.ApiConfig;
import com.example.labdata_main.api.dto.SyncExperimentTaskRequest;
import com.example.labdata_main.api.response.ApiResponse;
import com.example.labdata_main.model.ExperimentTask;
import com.example.labdata_main.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 数据同步服务类
 * 负责处理与后端的数据同步操作
 */
public class SyncService {
    private static final String TAG = "SyncService";

    /**
     * 同步实验任务到服务器
     * @param task 要同步的实验任务
     * @param listener 同步结果监听器
     */
    public static void syncExperimentTask(ExperimentTask task, SyncResultListener<ExperimentTask> listener) {
        // 先检查依赖项是否有效
        if (!validateDependencies(task)) {
            String errorMsg = "同步失败：缺少有效的依赖项（项目、配比、制件方法或原料）";
            Log.e(TAG, errorMsg);
            if (listener != null) {
                listener.onSyncFailure(400, errorMsg);
            }
            return;
        }
        
        // 创建适用于同步的DTO请求对象
        SyncExperimentTaskRequest request = SyncExperimentTaskRequest.fromExperimentTask(task, getUserOrganizationId());
        
        Log.d(TAG, "正在发送实验任务同步请求: " + task.getTaskName() + ", projectId: " + task.getProjectId());
        Log.d(TAG, "mixRatioId: " + (task.getMixRatioId() != null ? task.getMixRatioId() : "无") + ", mixingMethodId: " + (task.getMixingMethodId() != null ? task.getMixingMethodId() : "无"));
        
        // 记录技术信息便于调试
        if (task.getMaterialIds() != null && !task.getMaterialIds().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Long id : task.getMaterialIds()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(id);
            }
            Log.d(TAG, "materialIds: " + sb.toString());
        } else {
            Log.d(TAG, "materialIds: 空");
        }
        
        // 记录查询结构
        if (request.getMaterials() != null) {
            Log.d(TAG, "有效的后台原料对象数量: " + request.getMaterials().size());
        }

        if (request.getProject() != null) {
            Log.d(TAG, "项目ID: " + request.getProject().getId());
        }
        
        if (request.getMixRatio() != null) {
            Log.d(TAG, "配比ID: " + request.getMixRatio().getId());
        }
        
        if (request.getMixingMethod() != null) {
            Log.d(TAG, "制件方法ID: " + request.getMixingMethod().getId());
        }

        // 使用ApiClient发送同步请求
        ApiClient.getInstance().syncExperimentTask(request).enqueue(new Callback<ApiResponse<ExperimentTask>>() {
            @Override
            public void onResponse(Call<ApiResponse<ExperimentTask>> call, Response<ApiResponse<ExperimentTask>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ExperimentTask task = response.body().getData();
                    Log.d(TAG, "实验任务同步成功, 服务器ID: " + (task != null ? task.getId() : "null"));
                    if (listener != null && task != null) {
                        listener.onSyncSuccess(task);
                    }
                } else {
                    String errorMsg = "服务器返回错误: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n错误详情: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析错误响应失败", e);
                    }
                    Log.e(TAG, errorMsg);
                    if (listener != null) {
                        listener.onSyncFailure(response.code(), errorMsg);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ExperimentTask>> call, Throwable t) {
                String errorMsg = "请求失败: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                if (listener != null) {
                    listener.onSyncFailure(0, errorMsg);
                }
            }
        });
    }
    
    /**
     * 验证实验任务的依赖项是否有效
     * 这个方法检查项目、配比、混合方法和材料是否有有效的ID
     * @param task 要验证的实验任务
     * @return 如果所有依赖项都有效则返回true，否则返回false
     */
    private static boolean validateDependencies(ExperimentTask task) {
        // 检查项目ID
        if (task.getProjectId() <= 0) {
            Log.e(TAG, "缺少有效的项目ID");
            return false;
        }
        
        // 检查配比ID
        if (task.getMixRatioId() == null || task.getMixRatioId() <= 0) {
            Log.e(TAG, "缺少有效的配比ID");
            return false;
        }
        
        // 检查混合方法ID
        if (task.getMixingMethodId() == null || task.getMixingMethodId() <= 0) {
            Log.e(TAG, "缺少有效的混合方法ID");
            return false;
        }
        
        // 检查材料ID列表
        if (task.getMaterialIds() == null || task.getMaterialIds().isEmpty()) {
            Log.e(TAG, "缺少有效的材料ID");
            return false;
        }
        
        // 所有依赖项都有效
        return true;
    }

    /**
     * 同步结果回调接口
     */
    public interface SyncResultListener<T> {
        void onSyncSuccess(T result);
        void onSyncFailure(int code, String message);
    }
    
    // 获取当前用户的组织ID
    private static long getUserOrganizationId() {
        // 通过ApplicationContext获取SharedPrefsManager实例
        Context context = LabDataApplication.getAppContext();
        if (context == null) {
            // 如果无法获取上下文，返回默认值0
            Log.w(TAG, "无法获取应用上下文，使用默认组织ID 0");
            return 0;
        }
        
        SharedPrefsManager prefsManager = new SharedPrefsManager(context);
        // 使用用户ID作为组织ID的替代
        // 实际项目可能需要专门存储组织ID或从用户信息API获取
        long userId = prefsManager.getUserId();
        if (userId <= 0) {
            Log.w(TAG, "用户未登录或无效，使用默认组织ID 0");
            return 0;
        }
        
        Log.d(TAG, "当前用户组织ID: " + userId);
        return userId;
    }
}
