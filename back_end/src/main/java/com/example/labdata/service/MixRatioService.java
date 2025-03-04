package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.MixRatio;
import com.example.labdata.repository.MixRatioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MixRatioService {
    
    @Autowired
    private MixRatioRepository mixRatioRepository;
    
    public List<MixRatio> getAllMixRatios() {
        return mixRatioRepository.findAll();
    }
    
    public MixRatio getMixRatioById(Long id) {
        return mixRatioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MixRatio", "id", id));
    }
    
    public Optional<MixRatio> findByClientId(Long clientId) {
        return mixRatioRepository.findByClientId(clientId);
    }
    
    @Transactional
    public MixRatio createMixRatio(MixRatio mixRatio) {
        mixRatio.setSynced(true);
        mixRatio.setSyncStatus("SYNCED");
        return mixRatioRepository.save(mixRatio);
    }
    
    @Transactional
    public MixRatio updateMixRatio(Long id, MixRatio mixRatioDetails) {
        MixRatio mixRatio = getMixRatioById(id);
        
        mixRatio.setName(mixRatioDetails.getName());
        mixRatio.setDescription(mixRatioDetails.getDescription());
        mixRatio.setMaterials(mixRatioDetails.getMaterials());
        mixRatio.setMaterialPercentages(mixRatioDetails.getMaterialPercentages());
        mixRatio.setSynced(true);
        mixRatio.setSyncStatus("SYNCED");
        
        return mixRatioRepository.save(mixRatio);
    }
    
    @Transactional
    public void deleteMixRatio(Long id) {
        MixRatio mixRatio = getMixRatioById(id);
        mixRatioRepository.delete(mixRatio);
    }
    
    public List<MixRatio> findMixRatiosByMaterialId(Long materialId) {
        return mixRatioRepository.findByMaterialId(materialId);
    }
    
    public List<MixRatio> findModifiedSince(Instant lastSyncTime) {
        return mixRatioRepository.findModifiedSince(lastSyncTime);
    }
    
    @Transactional
    public MixRatio syncMixRatio(MixRatio mixRatio) {
        // 检查是否已存在对应的混合比例
        Optional<MixRatio> existingByClientId = mixRatioRepository.findByClientId(mixRatio.getClientId());
        
        if (existingByClientId.isPresent()) {
            // 更新现有混合比例
            MixRatio existing = existingByClientId.get();
            existing.setName(mixRatio.getName());
            existing.setDescription(mixRatio.getDescription());
            existing.setMaterials(mixRatio.getMaterials());
            existing.setMaterialPercentages(mixRatio.getMaterialPercentages());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            return mixRatioRepository.save(existing);
        } else {
            // 创建新混合比例
            mixRatio.setSynced(true);
            mixRatio.setSyncStatus("SYNCED");
            return mixRatioRepository.save(mixRatio);
        }
    }
}
