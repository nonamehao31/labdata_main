package com.example.labdata.service;

import com.example.labdata.model.SandMaterial;
import com.example.labdata.repository.SandMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class SandMaterialService {

    private final SandMaterialRepository sandMaterialRepository;

    @Autowired
    public SandMaterialService(SandMaterialRepository sandMaterialRepository) {
        this.sandMaterialRepository = sandMaterialRepository;
    }

    public List<SandMaterial> getAllSandMaterials() {
        return sandMaterialRepository.findAll();
    }
    
    public List<SandMaterial> getSandMaterialsByCompany(String company) {
        return sandMaterialRepository.findByCompany(company);
    }

    public SandMaterial getSandMaterialById(Long id) {
        return sandMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("沙子原料未找到，ID: " + id));
    }

    public SandMaterial createSandMaterial(SandMaterial sandMaterial) {
        return sandMaterialRepository.save(sandMaterial);
    }

    public SandMaterial updateSandMaterial(Long id, SandMaterial sandMaterialDetails) {
        SandMaterial sandMaterial = getSandMaterialById(id);
        
        sandMaterial.setName(sandMaterialDetails.getName());
        
        return sandMaterialRepository.save(sandMaterial);
    }

    public void deleteSandMaterial(Long id) {
        SandMaterial sandMaterial = getSandMaterialById(id);
        sandMaterialRepository.delete(sandMaterial);
    }
}
