package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.MixingMethod;
import com.example.labdata.repository.MixingMethodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MixingMethodService {

    private final MixingMethodRepository mixingMethodRepository;

    @Autowired
    public MixingMethodService(MixingMethodRepository mixingMethodRepository) {
        this.mixingMethodRepository = mixingMethodRepository;
    }

    public List<MixingMethod> getAllMixingMethodsByOrganization(Long organizationId) {
        return mixingMethodRepository.findByOrganizationId(organizationId);
    }

    public MixingMethod getMixingMethodById(Long id) {
        return mixingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MixingMethod", "id", id));
    }

    public MixingMethod getMixingMethodByClientId(Long clientId) {
        return mixingMethodRepository.findByClientId(clientId);
    }

    public MixingMethod createMixingMethod(MixingMethod mixingMethod) {
        return mixingMethodRepository.save(mixingMethod);
    }

    public MixingMethod updateMixingMethod(Long id, MixingMethod mixingMethodDetails) {
        MixingMethod mixingMethod = getMixingMethodById(id);
        
        mixingMethod.setName(mixingMethodDetails.getName());
        mixingMethod.setDescription(mixingMethodDetails.getDescription());
        mixingMethod.setSteps(mixingMethodDetails.getSteps());
        mixingMethod.setMixingTemperature(mixingMethodDetails.getMixingTemperature());
        mixingMethod.setMixingTime(mixingMethodDetails.getMixingTime());
        mixingMethod.setSynced(mixingMethodDetails.isSynced());
        
        return mixingMethodRepository.save(mixingMethod);
    }

    public void deleteMixingMethod(Long id) {
        MixingMethod mixingMethod = getMixingMethodById(id);
        mixingMethodRepository.delete(mixingMethod);
    }
}
