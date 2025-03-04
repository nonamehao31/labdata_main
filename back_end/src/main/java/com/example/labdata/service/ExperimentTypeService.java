package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.ExperimentType;
import com.example.labdata.repository.ExperimentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ExperimentTypeService {
    
    @Autowired
    private ExperimentTypeRepository experimentTypeRepository;
    
    public List<ExperimentType> getAllExperimentTypes() {
        return experimentTypeRepository.findAll();
    }
    
    public ExperimentType getExperimentTypeById(Long id) {
        return experimentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExperimentType", "id", id));
    }
    
    public Optional<ExperimentType> findByClientId(Long clientId) {
        return experimentTypeRepository.findByClientId(clientId);
    }
    
    @Transactional
    public ExperimentType createExperimentType(ExperimentType experimentType) {
        experimentType.setSynced(true);
        experimentType.setSyncStatus("SYNCED");
        return experimentTypeRepository.save(experimentType);
    }
    
    @Transactional
    public ExperimentType updateExperimentType(Long id, ExperimentType experimentTypeDetails) {
        ExperimentType experimentType = getExperimentTypeById(id);
        
        experimentType.setName(experimentTypeDetails.getName());
        experimentType.setDescription(experimentTypeDetails.getDescription());
        experimentType.setFields(experimentTypeDetails.getFields());
        experimentType.setSynced(true);
        experimentType.setSyncStatus("SYNCED");
        
        return experimentTypeRepository.save(experimentType);
    }
    
    @Transactional
    public void deleteExperimentType(Long id) {
        ExperimentType experimentType = getExperimentTypeById(id);
        experimentTypeRepository.delete(experimentType);
    }
    
    public List<ExperimentType> findModifiedSince(Instant lastSyncTime) {
        return experimentTypeRepository.findModifiedSince(lastSyncTime);
    }
    
    @Transactional
    public ExperimentType syncExperimentType(ExperimentType experimentType) {
        // 检查是否已存在对应的实验类型
        Optional<ExperimentType> existingByClientId = experimentTypeRepository.findByClientId(experimentType.getClientId());
        
        if (existingByClientId.isPresent()) {
            // 更新现有实验类型
            ExperimentType existing = existingByClientId.get();
            existing.setName(experimentType.getName());
            existing.setDescription(experimentType.getDescription());
            existing.setFields(experimentType.getFields());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            return experimentTypeRepository.save(existing);
        } else {
            // 创建新实验类型
            experimentType.setSynced(true);
            experimentType.setSyncStatus("SYNCED");
            return experimentTypeRepository.save(experimentType);
        }
    }
}
