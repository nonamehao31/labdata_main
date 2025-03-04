package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.Specimen;
import com.example.labdata.repository.SpecimenRepository;
import com.example.labdata.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class SpecimenService {
    
    @Autowired
    private SpecimenRepository specimenRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Specimen> getAllSpecimens() {
        return specimenRepository.findAll();
    }
    
    public Specimen getSpecimenById(Long id) {
        return specimenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specimen", "id", id));
    }
    
    public Optional<Specimen> findByCode(String code) {
        return specimenRepository.findByCode(code);
    }
    
    public Optional<Specimen> findByClientId(Long clientId) {
        return specimenRepository.findByClientId(clientId);
    }
    
    @Transactional
    public Specimen createSpecimen(Specimen specimen) {
        specimen.setSynced(true);
        specimen.setSyncStatus("SYNCED");
        Specimen savedSpecimen = specimenRepository.save(specimen);
        
        // 发送实体创建通知
        notificationService.notifyEntityCreated("specimen", savedSpecimen);
        
        return savedSpecimen;
    }
    
    @Transactional
    public Specimen updateSpecimen(Long id, Specimen specimenDetails) {
        Specimen specimen = getSpecimenById(id);
        
        specimen.setCode(specimenDetails.getCode());
        specimen.setMixRatio(specimenDetails.getMixRatio());
        specimen.setMixingTemperature(specimenDetails.getMixingTemperature());
        specimen.setMixingSpeed(specimenDetails.getMixingSpeed());
        specimen.setCompactionMethod(specimenDetails.getCompactionMethod());
        specimen.setCreationTime(specimenDetails.getCreationTime());
        specimen.setCutShape(specimenDetails.getCutShape());
        specimen.setCutCount(specimenDetails.getCutCount());
        specimen.setLength(specimenDetails.getLength());
        specimen.setWidth(specimenDetails.getWidth());
        specimen.setHeight(specimenDetails.getHeight());
        specimen.setRadius(specimenDetails.getRadius());
        specimen.setSynced(true);
        specimen.setSyncStatus("SYNCED");
        
        Specimen updatedSpecimen = specimenRepository.save(specimen);
        
        // 发送实体更新通知
        notificationService.notifyEntityUpdated("specimen", updatedSpecimen);
        
        return updatedSpecimen;
    }
    
    @Transactional
    public void deleteSpecimen(Long id) {
        Specimen specimen = getSpecimenById(id);
        specimenRepository.delete(specimen);
        
        // 发送实体删除通知
        notificationService.notifyEntityDeleted("specimen", specimen);
    }
    
    public List<Specimen> findSpecimensByMixRatioId(Long mixRatioId) {
        return specimenRepository.findByMixRatioId(mixRatioId);
    }
    
    public List<Specimen> findModifiedSince(Instant lastSyncTime) {
        return specimenRepository.findModifiedSince(lastSyncTime);
    }
    
    @Transactional
    public Specimen syncSpecimen(Specimen specimen) {
        // 检查是否已存在对应的样本
        Optional<Specimen> existingByClientId = specimenRepository.findByClientId(specimen.getClientId());
        Optional<Specimen> existingByCode = specimen.getCode() != null ? specimenRepository.findByCode(specimen.getCode()) : Optional.empty();
        
        Specimen result;
        
        if (existingByClientId.isPresent()) {
            // 更新现有样本
            Specimen existing = existingByClientId.get();
            existing.setCode(specimen.getCode());
            existing.setMixRatio(specimen.getMixRatio());
            existing.setMixingTemperature(specimen.getMixingTemperature());
            existing.setMixingSpeed(specimen.getMixingSpeed());
            existing.setCompactionMethod(specimen.getCompactionMethod());
            existing.setCreationTime(specimen.getCreationTime());
            existing.setCutShape(specimen.getCutShape());
            existing.setCutCount(specimen.getCutCount());
            existing.setLength(specimen.getLength());
            existing.setWidth(specimen.getWidth());
            existing.setHeight(specimen.getHeight());
            existing.setRadius(specimen.getRadius());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            result = specimenRepository.save(existing);
            
            // 发送实体更新通知
            notificationService.notifyEntityUpdated("specimen", result);
        } else if (existingByCode.isPresent()) {
            // 通过编码找到样本但客户端ID不同，可能是冲突
            Specimen existing = existingByCode.get();
            // 这里可以实现一些冲突解决策略
            // 例如，如果服务器版本比客户端更新，则保留服务器版本
            existing.setClientId(specimen.getClientId()); // 关联客户端ID
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            result = specimenRepository.save(existing);
            
            // 发送实体更新通知
            notificationService.notifyEntityUpdated("specimen", result);
        } else {
            // 创建新样本
            specimen.setSynced(true);
            specimen.setSyncStatus("SYNCED");
            result = specimenRepository.save(specimen);
            
            // 发送实体创建通知
            notificationService.notifyEntityCreated("specimen", result);
        }
        
        return result;
    }
}
