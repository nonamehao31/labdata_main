package com.example.labdata_main.api.service;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.api.response.AsphaltMaterialResponse;
import com.example.labdata_main.api.response.SandMaterialResponse;
import com.example.labdata_main.api.response.StoneMaterialResponse;
import com.example.labdata_main.database.DatabaseHelper;
import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.util.PreferenceManager;
import com.example.labdata_main.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 材料同步管理器 - 负责从后端API获取材料列表并同步到本地数据库
 */
public class MaterialSyncManager {
    private static final String TAG = "MaterialSyncManager";
    
    private final Context context;
    private final MaterialApiService apiService;
    private final DatabaseHelper databaseHelper;
    private final ExecutorService executorService;
    private final String companyId;
    
    public interface SyncCallback {
        void onSyncComplete(boolean success, String message);
    }
    
    public MaterialSyncManager(Context context) {
        this.context = context;
        this.apiService = new MaterialApiService();
        this.databaseHelper = DatabaseHelper.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
        
        // 使用SharedPrefsManager获取公司ID，确保与登录逻辑一致
        // 注意：SharedPrefsManager存储在utils包中，PreferenceManager存储在util包中
        com.example.labdata_main.utils.SharedPrefsManager sharedPrefsManager = new com.example.labdata_main.utils.SharedPrefsManager(context);
        this.companyId = sharedPrefsManager.getUserCompany();
        Log.d(TAG, "初始化MaterialSyncManager，公司ID: " + companyId);
    }
    
    /**
     * 同步所有类型的材料数据
     * @param callback 同步完成回调
     */
    public void syncAllMaterials(final SyncCallback callback) {
        Log.d(TAG, "开始同步所有材料数据");
        
        final AtomicInteger completedCount = new AtomicInteger(0);
        final AtomicInteger successCount = new AtomicInteger(0);
        final StringBuilder errorMessages = new StringBuilder();
        
        // 同步沥青材料
        syncAsphaltMaterials(new SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    successCount.incrementAndGet();
                } else {
                    errorMessages.append("沥青材料同步失败: ").append(message).append("\n");
                }
                
                if (completedCount.incrementAndGet() == 3) {
                    // 所有同步任务完成
                    if (successCount.get() == 3) {
                        callback.onSyncComplete(true, "所有材料同步成功");
                    } else {
                        callback.onSyncComplete(false, errorMessages.toString());
                    }
                }
            }
        });
        
        // 同步沙子材料
        syncSandMaterials(new SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    successCount.incrementAndGet();
                } else {
                    errorMessages.append("沙子材料同步失败: ").append(message).append("\n");
                }
                
                if (completedCount.incrementAndGet() == 3) {
                    // 所有同步任务完成
                    if (successCount.get() == 3) {
                        callback.onSyncComplete(true, "所有材料同步成功");
                    } else {
                        callback.onSyncComplete(false, errorMessages.toString());
                    }
                }
            }
        });
        
        // 同步石子材料
        syncStoneMaterials(new SyncCallback() {
            @Override
            public void onSyncComplete(boolean success, String message) {
                if (success) {
                    successCount.incrementAndGet();
                } else {
                    errorMessages.append("石子材料同步失败: ").append(message).append("\n");
                }
                
                if (completedCount.incrementAndGet() == 3) {
                    // 所有同步任务完成
                    if (successCount.get() == 3) {
                        callback.onSyncComplete(true, "所有材料同步成功");
                    } else {
                        callback.onSyncComplete(false, errorMessages.toString());
                    }
                }
            }
        });
    }
    
    /**
     * 同步沥青材料
     * @param callback 同步完成回调
     */
    public void syncAsphaltMaterials(final SyncCallback callback) {
        Log.d(TAG, "开始同步沥青材料，公司ID: " + companyId);
        
        apiService.getAllAsphaltMaterials(companyId, new MaterialApiService.ApiCallback<List<AsphaltMaterialResponse>>() {
            @Override
            public void onSuccess(final List<AsphaltMaterialResponse> data) {
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "从API获取到0个沥青材料");
                    callback.onSyncComplete(true, "没有沥青材料需要同步");
                    return;
                }
                
                Log.d(TAG, "从API获取到" + data.size() + "个沥青材料");
                
                // 在后台线程中保存到数据库
                executorService.execute(() -> {
                    try {
                        List<MaterialProperty> properties = new ArrayList<>();
                        
                        for (AsphaltMaterialResponse item : data) {
                            MaterialProperty property = new MaterialProperty();
                            property.setName(item.getName());
                            property.setType("asphalt");
                            property.setCode(String.valueOf(item.getId()));
                            property.setGrade(item.getGrade());
                            property.setCharacter(item.getCharacter());
                            // 设置服务器ID
                            property.setServerId(item.getId());
                            properties.add(property);
                        }
                        
                        // 保存到数据库
                        databaseHelper.saveOrUpdateMaterialProperties(properties);
                        
                        Log.d(TAG, "沥青材料同步成功, 保存了" + properties.size() + "个材料到本地数据库");
                        callback.onSyncComplete(true, "沥青材料同步成功");
                    } catch (Exception e) {
                        Log.e(TAG, "保存沥青材料到本地数据库失败", e);
                        callback.onSyncComplete(false, "保存到本地数据库失败: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "获取沥青材料列表失败: " + errorMessage);
                callback.onSyncComplete(false, errorMessage);
            }
        });
    }
    
    /**
     * 同步沙子材料
     * @param callback 同步完成回调
     */
    public void syncSandMaterials(final SyncCallback callback) {
        Log.d(TAG, "开始同步沙子材料，公司ID: " + companyId);
        
        apiService.getAllSandMaterials(companyId, new MaterialApiService.ApiCallback<List<SandMaterialResponse>>() {
            @Override
            public void onSuccess(final List<SandMaterialResponse> data) {
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "从API获取到0个沙子材料");
                    callback.onSyncComplete(true, "没有沙子材料需要同步");
                    return;
                }
                
                Log.d(TAG, "从API获取到" + data.size() + "个沙子材料");
                
                // 在后台线程中保存到数据库
                executorService.execute(() -> {
                    try {
                        List<MaterialProperty> properties = new ArrayList<>();
                        
                        for (SandMaterialResponse item : data) {
                            MaterialProperty property = new MaterialProperty();
                            property.setName(item.getName());
                            property.setType("sand");
                            property.setCode(String.valueOf(item.getId()));
                            // 设置服务器ID
                            property.setServerId(item.getId());
                            properties.add(property);
                        }
                        
                        // 保存到数据库
                        databaseHelper.saveOrUpdateMaterialProperties(properties);
                        
                        Log.d(TAG, "沙子材料同步成功, 保存了" + properties.size() + "个材料到本地数据库");
                        callback.onSyncComplete(true, "沙子材料同步成功");
                    } catch (Exception e) {
                        Log.e(TAG, "保存沙子材料到本地数据库失败", e);
                        callback.onSyncComplete(false, "保存到本地数据库失败: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "获取沙子材料列表失败: " + errorMessage);
                callback.onSyncComplete(false, errorMessage);
            }
        });
    }
    
    /**
     * 同步石子材料
     * @param callback 同步完成回调
     */
    public void syncStoneMaterials(final SyncCallback callback) {
        Log.d(TAG, "开始同步石子材料，公司ID: " + companyId);
        
        apiService.getAllStoneMaterials(companyId, new MaterialApiService.ApiCallback<List<StoneMaterialResponse>>() {
            @Override
            public void onSuccess(final List<StoneMaterialResponse> data) {
                if (data == null || data.isEmpty()) {
                    Log.d(TAG, "从API获取到0个石子材料");
                    callback.onSyncComplete(true, "没有石子材料需要同步");
                    return;
                }
                
                Log.d(TAG, "从API获取到" + data.size() + "个石子材料");
                
                // 在后台线程中保存到数据库
                executorService.execute(() -> {
                    try {
                        List<MaterialProperty> properties = new ArrayList<>();
                        
                        for (StoneMaterialResponse item : data) {
                            MaterialProperty property = new MaterialProperty();
                            property.setName(item.getName());
                            property.setType("stone");
                            property.setCode(String.valueOf(item.getId()));
                            // 设置服务器ID
                            property.setServerId(item.getId());
                            properties.add(property);
                        }
                        
                        // 保存到数据库
                        databaseHelper.saveOrUpdateMaterialProperties(properties);
                        
                        Log.d(TAG, "石子材料同步成功, 保存了" + properties.size() + "个材料到本地数据库");
                        callback.onSyncComplete(true, "石子材料同步成功");
                    } catch (Exception e) {
                        Log.e(TAG, "保存石子材料到本地数据库失败", e);
                        callback.onSyncComplete(false, "保存到本地数据库失败: " + e.getMessage());
                    }
                });
            }
            
            @Override
            public void onFailure(String errorMessage) {
                Log.e(TAG, "获取石子材料列表失败: " + errorMessage);
                callback.onSyncComplete(false, errorMessage);
            }
        });
    }
    
    /**
     * 同步指定类型的材料
     * @param materialType 材料类型 (asphalt, sand, stone)
     * @param callback 同步完成回调
     */
    public void syncMaterialsByType(String materialType, final SyncCallback callback) {
        switch (materialType) {
            case "asphalt":
                syncAsphaltMaterials(callback);
                break;
            case "sand":
                syncSandMaterials(callback);
                break;
            case "stone":
                syncStoneMaterials(callback);
                break;
            default:
                callback.onSyncComplete(false, "不支持的材料类型: " + materialType);
                break;
        }
    }
    
    /**
     * 释放资源
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
