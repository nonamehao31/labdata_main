package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.Material;
import com.example.labdata.repository.MaterialRepository;
import com.example.labdata.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MaterialService {
    
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<Material> getAllMaterials() {
        return materialRepository.findAll();
    }
    
    public List<Material> getMaterialsByOrganization(Long organizationId) {
        return materialRepository.findByOrganizationId(organizationId);
    }
    
    public List<Material> getMaterialsByTypeAndOrganization(String type, Long organizationId) {
        return materialRepository.findByTypeAndOrganizationId(type, organizationId);
    }
    
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material", "id", id));
    }
    
    public Optional<Material> findByClientId(Long clientId) {
        return materialRepository.findByClientId(clientId);
    }
    
    @Transactional
    public Material createMaterial(Material material) {
        material.setSynced(true);
        material.setSyncStatus("SYNCED");
        Material savedMaterial = materialRepository.save(material);
        
        // 发送实体创建通知
        notificationService.notifyEntityCreated("material", savedMaterial);
        
        return savedMaterial;
    }
    
    @Transactional
    public Material updateMaterial(Long id, Material materialDetails) {
        Material material = getMaterialById(id);
        
        material.setName(materialDetails.getName());
        material.setDescription(materialDetails.getDescription());
        material.setType(materialDetails.getType());
        material.setDensity(materialDetails.getDensity());
        material.setProperties(materialDetails.getProperties());
        material.setSynced(true);
        material.setSyncStatus("SYNCED");
        
        Material updatedMaterial = materialRepository.save(material);
        
        // 发送实体更新通知
        notificationService.notifyEntityUpdated("material", updatedMaterial);
        
        return updatedMaterial;
    }
    
    @Transactional
    public void deleteMaterial(Long id) {
        Material material = getMaterialById(id);
        materialRepository.delete(material);
        
        // 发送实体删除通知
        notificationService.notifyEntityDeleted("material", material);
    }
    
    public List<Material> findMaterialsByType(String type) {
        return materialRepository.findByType(type);
    }
    
    public List<Material> findModifiedSince(Instant lastSyncTime) {
        return materialRepository.findModifiedSince(lastSyncTime);
    }
    
    public List<Material> findModifiedSince(Instant lastSyncTime, Long organizationId) {
        // 按组织ID和最后同步时间过滤材料
        List<Material> modifiedMaterials = materialRepository.findModifiedSince(lastSyncTime);
        return modifiedMaterials.stream()
                .filter(material -> material.getOrganizationId() == null || material.getOrganizationId().equals(organizationId))
                .toList();
    }
    
    @Transactional
    public Material syncMaterial(Material material) {
        // 检查是否已存在对应的材料
        Optional<Material> existingByClientId = materialRepository.findByClientId(material.getClientId());
        
        Material result;
        boolean isNew = !existingByClientId.isPresent();
        
        if (existingByClientId.isPresent()) {
            // 更新现有材料
            Material existing = existingByClientId.get();
            existing.setName(material.getName());
            existing.setDescription(material.getDescription());
            existing.setType(material.getType());
            existing.setDensity(material.getDensity());
            existing.setProperties(material.getProperties());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            result = materialRepository.save(existing);
            
            // 发送实体更新通知
            notificationService.notifyEntityUpdated("material", result);
        } else {
            // 创建新材料
            material.setSynced(true);
            material.setSyncStatus("SYNCED");
            result = materialRepository.save(material);
            
            // 发送实体创建通知
            notificationService.notifyEntityCreated("material", result);
        }
        
        return result;
    }
}
