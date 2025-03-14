package com.example.labdata.service;

import com.example.labdata.model.AsphaltMaterial;
import com.example.labdata.repository.AsphaltMaterialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class AsphaltMaterialService {

    private final AsphaltMaterialRepository asphaltMaterialRepository;

    @Autowired
    public AsphaltMaterialService(AsphaltMaterialRepository asphaltMaterialRepository) {
        this.asphaltMaterialRepository = asphaltMaterialRepository;
    }

    public List<AsphaltMaterial> getAllAsphaltMaterials() {
        return asphaltMaterialRepository.findAll();
    }

    public List<AsphaltMaterial> getAsphaltMaterialsByCompany(String company) {
        return asphaltMaterialRepository.findByCompany(company);
    }

    public AsphaltMaterial getAsphaltMaterialById(Long id) {
        return asphaltMaterialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("沥青原料未找到，ID: " + id));
    }

    public AsphaltMaterial createAsphaltMaterial(AsphaltMaterial asphaltMaterial) {
        return asphaltMaterialRepository.save(asphaltMaterial);
    }

    public AsphaltMaterial updateAsphaltMaterial(Long id, AsphaltMaterial asphaltMaterialDetails) {
        AsphaltMaterial asphaltMaterial = getAsphaltMaterialById(id);
        
        asphaltMaterial.setName(asphaltMaterialDetails.getName());
        asphaltMaterial.setGrade(asphaltMaterialDetails.getGrade());
        asphaltMaterial.setCharacter(asphaltMaterialDetails.getCharacter());
        
        return asphaltMaterialRepository.save(asphaltMaterial);
    }

    public void deleteAsphaltMaterial(Long id) {
        AsphaltMaterial asphaltMaterial = getAsphaltMaterialById(id);
        asphaltMaterialRepository.delete(asphaltMaterial);
    }
}
