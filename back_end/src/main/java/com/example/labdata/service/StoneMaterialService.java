package com.example.labdata.service;

import com.example.labdata.model.StoneMaterial;
import com.example.labdata.repository.StoneMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class StoneMaterialService {

    private final StoneMaterialRepository stoneMaterialRepository;

    @Autowired
    public StoneMaterialService(StoneMaterialRepository stoneMaterialRepository) {
        this.stoneMaterialRepository = stoneMaterialRepository;
    }

    public List<StoneMaterial> getAllStoneMaterials() {
        return stoneMaterialRepository.findAll();
    }
    
    public List<StoneMaterial> getStoneMaterialsByCompany(String company) {
        return stoneMaterialRepository.findByCompany(company);
    }

    public StoneMaterial getStoneMaterialById(Long id) {
        return stoneMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("石子原料未找到，ID: " + id));
    }

    public StoneMaterial createStoneMaterial(StoneMaterial stoneMaterial) {
        return stoneMaterialRepository.save(stoneMaterial);
    }

    public StoneMaterial updateStoneMaterial(Long id, StoneMaterial stoneMaterialDetails) {
        StoneMaterial stoneMaterial = getStoneMaterialById(id);
        
        stoneMaterial.setName(stoneMaterialDetails.getName());
        
        return stoneMaterialRepository.save(stoneMaterial);
    }

    public void deleteStoneMaterial(Long id) {
        StoneMaterial stoneMaterial = getStoneMaterialById(id);
        stoneMaterialRepository.delete(stoneMaterial);
    }
}
