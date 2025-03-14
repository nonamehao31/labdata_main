package com.example.labdata_main.api.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.labdata_main.api.response.AsphaltMaterialResponse;
import com.example.labdata_main.api.response.SandMaterialResponse;
import com.example.labdata_main.api.response.StoneMaterialResponse;
import com.example.labdata_main.model.MaterialItem;

/**
 * 材料同步服务，处理前端与后端API的材料数据同步
 */
public class MaterialSyncService {
    private static final String TAG = "MaterialSyncService";
    
    private final MaterialApiService materialApiService;
    private final Context context;
    
    // 材料类型常量
    public static final String TYPE_ASPHALT = "沥青";
    public static final String TYPE_SAND = "沙子";
    public static final String TYPE_STONE = "石子";
    
    public MaterialSyncService(Context context) {
        this.context = context;
        this.materialApiService = new MaterialApiService();
    }
    
    /**
     * 同步材料到后端，根据材料类型调用相应的API
     * @param material 材料项
     * @param listener 同步结果监听器
     */
    public void syncMaterial(MaterialItem material, final SyncResultListener listener) {
        if (material == null || material.getName() == null) {
            if (listener != null) {
                listener.onSyncFailed("材料信息不完整");
            }
            return;
        }
        
        String materialName = material.getName();
        String materialType = material.getType();
        
        // 根据材料类型调用不同的API方法
        if (TYPE_ASPHALT.equals(materialType)) {
            // 对于沥青材料，假设grade和character从名称中解析
            // 实际应用中可能需要更复杂的逻辑或UI来获取这些值
            String grade = "70#"; // 默认值
            String character = "60/80"; // 默认值
            String companyId = "unknown"; // 默认值
            
            // 将character转换为API需要的格式
            String apiCharacter = "NORMAL"; // 默认为普通沥青
            
            materialApiService.saveAsphaltMaterial(materialName, grade, apiCharacter, companyId,
                new MaterialApiService.ApiCallback<AsphaltMaterialResponse>() {
                    @Override
                    public void onSuccess(AsphaltMaterialResponse data) {
                        if (listener != null) {
                            listener.onSyncSuccess(material, data.getId());
                        }
                    }
                    
                    @Override
                    public void onFailure(String errorMessage) {
                        if (listener != null) {
                            listener.onSyncFailed(errorMessage);
                        }
                    }
                });
        } 
        else if (TYPE_SAND.equals(materialType)) {
            materialApiService.saveSandMaterial(materialName, 
                new MaterialApiService.ApiCallback<SandMaterialResponse>() {
                    @Override
                    public void onSuccess(SandMaterialResponse data) {
                        if (listener != null) {
                            listener.onSyncSuccess(material, data.getId());
                        }
                    }
                    
                    @Override
                    public void onFailure(String errorMessage) {
                        if (listener != null) {
                            listener.onSyncFailed(errorMessage);
                        }
                    }
                });
        } 
        else if (TYPE_STONE.equals(materialType)) {
            materialApiService.saveStoneMaterial(materialName, 
                new MaterialApiService.ApiCallback<StoneMaterialResponse>() {
                    @Override
                    public void onSuccess(StoneMaterialResponse data) {
                        if (listener != null) {
                            listener.onSyncSuccess(material, data.getId());
                        }
                    }
                    
                    @Override
                    public void onFailure(String errorMessage) {
                        if (listener != null) {
                            listener.onSyncFailed(errorMessage);
                        }
                    }
                });
        } 
        else {
            Log.w(TAG, "不支持的材料类型: " + materialType);
            if (listener != null) {
                listener.onSyncFailed("不支持的材料类型: " + materialType);
            }
        }
    }
    
    /**
     * 自动识别材料类型并同步
     * 此方法通过材料名称尝试自动判断材料类型并调用相应API
     * @param material 材料项
     * @param listener 同步结果监听器
     */
    public void autoSyncMaterial(MaterialItem material, final SyncResultListener listener) {
        if (material == null || material.getName() == null) {
            if (listener != null) {
                listener.onSyncFailed("材料信息不完整");
            }
            return;
        }
        
        String materialName = material.getName().toLowerCase();
        String materialType = material.getType();
        
        // 如果已经指定了精确的材料类型，则直接使用
        if (TYPE_ASPHALT.equals(materialType) || 
            TYPE_SAND.equals(materialType) || 
            TYPE_STONE.equals(materialType)) {
            syncMaterial(material, listener);
            return;
        }
        
        // 通过名称关键词自动判断材料类型
        if (materialName.contains("沥青") || materialName.contains("油")) {
            material.setType(TYPE_ASPHALT);
            syncMaterial(material, listener);
        } 
        else if (materialName.contains("沙") || materialName.contains("砂")) {
            material.setType(TYPE_SAND);
            syncMaterial(material, listener);
        } 
        else if (materialName.contains("石") || materialName.contains("碎石") || 
                 materialName.contains("砾")) {
            material.setType(TYPE_STONE);
            syncMaterial(material, listener);
        } 
        else {
            // 无法判断类型时，尝试作为沥青材料同步（可以根据实际需求调整默认类型）
            Log.w(TAG, "无法判断材料类型，默认作为沥青材料处理: " + materialName);
            material.setType(TYPE_ASPHALT);
            syncMaterial(material, listener);
        }
    }
    
    /**
     * 同步结果监听器接口
     */
    public interface SyncResultListener {
        void onSyncSuccess(MaterialItem material, Long remoteId);
        void onSyncFailed(String errorMessage);
    }
}
