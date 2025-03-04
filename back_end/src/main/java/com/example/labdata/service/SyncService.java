package com.example.labdata.service;

import com.example.labdata.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供全局数据同步功能的服务
 */
@Service
public class SyncService {

    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private MixRatioService mixRatioService;
    
    @Autowired
    private SpecimenService specimenService;
    
    @Autowired
    private ExperimentTypeService experimentTypeService;
    
    @Autowired
    private ExperimentTaskService experimentTaskService;
    
    @Autowired
    private ExperimentDataService experimentDataService;

    /**
     * 获取自上次同步以来的所有更改数据
     */
    public Map<String, Object> getAllChangesSince(Instant lastSyncTime) {
        Map<String, Object> changes = new HashMap<>();
        
        // 获取所有已更改的材料
        List<Material> materials = materialService.findModifiedSince(lastSyncTime);
        changes.put("materials", materials);
        
        // 获取所有已更改的混合比例
        List<MixRatio> mixRatios = mixRatioService.findModifiedSince(lastSyncTime);
        changes.put("mixRatios", mixRatios);
        
        // 获取所有已更改的样本
        List<Specimen> specimens = specimenService.findModifiedSince(lastSyncTime);
        changes.put("specimens", specimens);
        
        // 获取所有已更改的实验类型
        List<ExperimentType> experimentTypes = experimentTypeService.findModifiedSince(lastSyncTime);
        changes.put("experimentTypes", experimentTypes);
        
        // 获取所有已更改的实验任务
        List<ExperimentTask> experimentTasks = experimentTaskService.findModifiedSince(lastSyncTime);
        changes.put("experimentTasks", experimentTasks);
        
        // 获取所有已更改的实验数据
        List<ExperimentData> experimentData = experimentDataService.findModifiedSince(lastSyncTime);
        changes.put("experimentData", experimentData);
        
        return changes;
    }

    /**
     * 处理客户端提交的批量同步数据
     */
    @Transactional
    public Map<String, Object> processSyncData(Map<String, Object> syncData) {
        Map<String, Object> result = new HashMap<>();
        
        // 处理材料同步
        if (syncData.containsKey("materials")) {
            List<Material> materials = (List<Material>) syncData.get("materials");
            List<Material> syncedMaterials = materials.stream()
                    .map(materialService::syncMaterial)
                    .toList();
            result.put("materials", syncedMaterials);
        }
        
        // 处理混合比例同步
        if (syncData.containsKey("mixRatios")) {
            List<MixRatio> mixRatios = (List<MixRatio>) syncData.get("mixRatios");
            List<MixRatio> syncedMixRatios = mixRatios.stream()
                    .map(mixRatioService::syncMixRatio)
                    .toList();
            result.put("mixRatios", syncedMixRatios);
        }
        
        // 处理样本同步
        if (syncData.containsKey("specimens")) {
            List<Specimen> specimens = (List<Specimen>) syncData.get("specimens");
            List<Specimen> syncedSpecimens = specimens.stream()
                    .map(specimenService::syncSpecimen)
                    .toList();
            result.put("specimens", syncedSpecimens);
        }
        
        // 处理实验类型同步
        if (syncData.containsKey("experimentTypes")) {
            List<ExperimentType> experimentTypes = (List<ExperimentType>) syncData.get("experimentTypes");
            List<ExperimentType> syncedExperimentTypes = experimentTypes.stream()
                    .map(experimentTypeService::syncExperimentType)
                    .toList();
            result.put("experimentTypes", syncedExperimentTypes);
        }
        
        // 处理实验任务同步
        if (syncData.containsKey("experimentTasks")) {
            List<ExperimentTask> experimentTasks = (List<ExperimentTask>) syncData.get("experimentTasks");
            List<ExperimentTask> syncedExperimentTasks = experimentTasks.stream()
                    .map(experimentTaskService::syncExperimentTask)
                    .toList();
            result.put("experimentTasks", syncedExperimentTasks);
        }
        
        // 处理实验数据同步
        if (syncData.containsKey("experimentData")) {
            List<ExperimentData> experimentData = (List<ExperimentData>) syncData.get("experimentData");
            List<ExperimentData> syncedExperimentData = experimentData.stream()
                    .map(experimentDataService::syncExperimentData)
                    .toList();
            result.put("experimentData", syncedExperimentData);
        }
        
        return result;
    }
}
