package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.ExperimentData;
import com.example.labdata.repository.ExperimentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ExperimentDataService {
    
    @Autowired
    private ExperimentDataRepository experimentDataRepository;
    
    public List<ExperimentData> getAllExperimentData() {
        return experimentDataRepository.findAll();
    }
    
    public ExperimentData getExperimentDataById(Long id) {
        return experimentDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExperimentData", "id", id));
    }
    
    public Optional<ExperimentData> findByClientId(Long clientId) {
        return experimentDataRepository.findByClientId(clientId);
    }
    
    @Transactional
    public ExperimentData createExperimentData(ExperimentData experimentData) {
        experimentData.setSynced(true);
        experimentData.setSyncStatus("SYNCED");
        return experimentDataRepository.save(experimentData);
    }
    
    @Transactional
    public ExperimentData updateExperimentData(Long id, ExperimentData experimentDataDetails) {
        ExperimentData experimentData = getExperimentDataById(id);
        
        experimentData.setExperimentTask(experimentDataDetails.getExperimentTask());
        experimentData.setDataValues(experimentDataDetails.getDataValues());
        experimentData.setNotes(experimentDataDetails.getNotes());
        experimentData.setSynced(true);
        experimentData.setSyncStatus("SYNCED");
        
        return experimentDataRepository.save(experimentData);
    }
    
    @Transactional
    public void deleteExperimentData(Long id) {
        ExperimentData experimentData = getExperimentDataById(id);
        experimentDataRepository.delete(experimentData);
    }
    
    public List<ExperimentData> findExperimentDataByTaskId(Long taskId) {
        return experimentDataRepository.findByExperimentTaskId(taskId);
    }
    
    public List<ExperimentData> findModifiedSince(Instant lastSyncTime) {
        return experimentDataRepository.findModifiedSince(lastSyncTime);
    }
    
    public List<ExperimentData> findByFieldNameAndValue(String fieldName, String value) {
        return experimentDataRepository.findByFieldNameAndValue(fieldName, value);
    }
    
    @Transactional
    public ExperimentData syncExperimentData(ExperimentData experimentData) {
        // 检查是否已存在对应的实验数据
        Optional<ExperimentData> existingByClientId = experimentDataRepository.findByClientId(experimentData.getClientId());
        
        if (existingByClientId.isPresent()) {
            // 更新现有实验数据
            ExperimentData existing = existingByClientId.get();
            existing.setExperimentTask(experimentData.getExperimentTask());
            existing.setDataValues(experimentData.getDataValues());
            existing.setNotes(experimentData.getNotes());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            return experimentDataRepository.save(existing);
        } else {
            // 创建新实验数据
            experimentData.setSynced(true);
            experimentData.setSyncStatus("SYNCED");
            return experimentDataRepository.save(experimentData);
        }
    }
}
