package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.ExperimentTask;
import com.example.labdata.model.Material;
import com.example.labdata.model.MixingMethod;
import com.example.labdata.model.MixRatio;
import com.example.labdata.model.Project;
import com.example.labdata.repository.ExperimentTaskRepository;
import com.example.labdata.repository.MaterialRepository;
import com.example.labdata.repository.MixingMethodRepository;
import com.example.labdata.repository.MixRatioRepository;
import com.example.labdata.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ExperimentTaskService {
    
    @Autowired
    private ExperimentTaskRepository experimentTaskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private MixingMethodRepository mixingMethodRepository;
    
    @Autowired
    private MixRatioRepository mixRatioRepository;
    
    @Autowired
    private MaterialRepository materialRepository;
    
    public List<ExperimentTask> getAllExperimentTasks() {
        return experimentTaskRepository.findAll();
    }
    
    public ExperimentTask getExperimentTaskById(Long id) {
        return experimentTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExperimentTask", "id", id));
    }
    
    public Optional<ExperimentTask> findByClientId(Long clientId) {
        return experimentTaskRepository.findByClientId(clientId);
    }
    
    @Transactional
    public ExperimentTask createExperimentTask(ExperimentTask experimentTask) {
        experimentTask.setSynced(true);
        experimentTask.setSyncStatus("SYNCED");
        return experimentTaskRepository.save(experimentTask);
    }
    
    @Transactional
    public ExperimentTask updateExperimentTask(Long id, ExperimentTask experimentTaskDetails) {
        ExperimentTask experimentTask = getExperimentTaskById(id);
        
        experimentTask.setName(experimentTaskDetails.getName());
        experimentTask.setDescription(experimentTaskDetails.getDescription());
        experimentTask.setExperimentType(experimentTaskDetails.getExperimentType());
        experimentTask.setSpecimen(experimentTaskDetails.getSpecimen());
        experimentTask.setAssignedTo(experimentTaskDetails.getAssignedTo());
        experimentTask.setScheduledStartTime(experimentTaskDetails.getScheduledStartTime());
        experimentTask.setScheduledEndTime(experimentTaskDetails.getScheduledEndTime());
        experimentTask.setActualStartTime(experimentTaskDetails.getActualStartTime());
        experimentTask.setActualEndTime(experimentTaskDetails.getActualEndTime());
        experimentTask.setStatus(experimentTaskDetails.getStatus());
        
        // 
        experimentTask.setProject(experimentTaskDetails.getProject());
        experimentTask.setMixingMethod(experimentTaskDetails.getMixingMethod());
        experimentTask.setMixRatio(experimentTaskDetails.getMixRatio());
        experimentTask.setMaterials(experimentTaskDetails.getMaterials());
        experimentTask.setOrganizationId(experimentTaskDetails.getOrganizationId());
        
        experimentTask.setSynced(true);
        experimentTask.setSyncStatus("SYNCED");
        
        return experimentTaskRepository.save(experimentTask);
    }
    
    @Transactional
    public void deleteExperimentTask(Long id) {
        ExperimentTask experimentTask = getExperimentTaskById(id);
        experimentTaskRepository.delete(experimentTask);
    }
    
    public List<ExperimentTask> findExperimentTasksByUserId(Long userId) {
        return experimentTaskRepository.findByAssignedToId(userId);
    }
    
    public List<ExperimentTask> findExperimentTasksBySpecimenId(Long specimenId) {
        return experimentTaskRepository.findBySpecimenId(specimenId);
    }
    
    public List<ExperimentTask> findExperimentTasksByStatus(ExperimentTask.TaskStatus status) {
        return experimentTaskRepository.findByStatus(status);
    }
    
    public List<ExperimentTask> findExperimentTasksByDateRange(Instant start, Instant end) {
        return experimentTaskRepository.findByScheduledStartTimeBetween(start, end);
    }
    
    public List<ExperimentTask> findModifiedSince(Instant lastSyncTime) {
        return experimentTaskRepository.findModifiedSince(lastSyncTime);
    }
    
    /**
     * u540cu6b65u9879u76eeu4fe1u606f - u5982u679cu5b58u5728u5219u66f4u65b0uff0cu4e0du5b58u5728u5219u521bu5efa
     * @param project u9879u76eeu4fe1u606f
     * @return u540cu6b65u540eu7684u9879u76eeu5b9eu4f53
     */
    @Transactional
    public Project syncProject(Project project) {
        if (project == null) return null;
        
        // u5148u6839u636eu5ba2u6237u7aefIDu67e5u627eu73b0u6709u9879u76ee
        Optional<Project> existingProject = null;
        
        // u5982u679cu6709IDuff0cu5148u6839u636eIDu67e5u627e
        if (project.getId() != null) {
            existingProject = projectRepository.findById(project.getId());
        }
        
        // u5982u679cu627eu4e0du5230uff0cu5219u6839u636eu540du79f0u548cu7ec4u7ec7IDu67e5u627e
        if (existingProject == null || !existingProject.isPresent()) {
            existingProject = projectRepository.findByNameAndOrganizationId(
                project.getName(), project.getOrganizationId());
        }
        
        if (existingProject != null && existingProject.isPresent()) {
            // u66f4u65b0u73b0u6709u9879u76ee
            Project existing = existingProject.get();
            existing.setName(project.getName());
            if (project.getDescription() != null) {
                existing.setDescription(project.getDescription());
            }
            return projectRepository.save(existing);
        } else {
            // u521bu5efau65b0u9879u76ee
            return projectRepository.save(project);
        }
    }
    
    /**
     * u540cu6b65u6df7u5408u65b9u6cd5u4fe1u606f - u5982u679cu5b58u5728u5219u66f4u65b0uff0cu4e0du5b58u5728u5219u521bu5efa
     * @param mixingMethod u6df7u5408u65b9u6cd5u4fe1u606f
     * @return u540cu6b65u540eu7684u6df7u5408u65b9u6cd5u5b9eu4f53
     */
    @Transactional
    public MixingMethod syncMixingMethod(MixingMethod mixingMethod) {
        if (mixingMethod == null) return null;
        
        // u5148u6839u636eu5ba2u6237u7aefIDu67e5u627eu73b0u6709u6df7u5408u65b9u6cd5
        Optional<MixingMethod> existingMethod = null;
        
        // u5982u679cu6709IDuff0cu5148u6839u636eIDu67e5u627e
        if (mixingMethod.getId() != null) {
            existingMethod = mixingMethodRepository.findById(mixingMethod.getId());
        }
        
        // u5982u679cu627eu4e0du5230uff0cu5219u6839u636eu540du79f0u548cu7ec4u7ec7IDu67e5u627e
        if (existingMethod == null || !existingMethod.isPresent()) {
            existingMethod = mixingMethodRepository.findByNameAndOrganizationId(
                mixingMethod.getName(), mixingMethod.getOrganizationId());
        }
        
        if (existingMethod != null && existingMethod.isPresent()) {
            // u66f4u65b0u73b0u6709u6df7u5408u65b9u6cd5
            MixingMethod existing = existingMethod.get();
            existing.setName(mixingMethod.getName());
            if (mixingMethod.getDescription() != null) {
                existing.setDescription(mixingMethod.getDescription());
            }
            return mixingMethodRepository.save(existing);
        } else {
            // u521bu5efau65b0u6df7u5408u65b9u6cd5
            return mixingMethodRepository.save(mixingMethod);
        }
    }
    
    /**
     * u540cu6b65u914du6bd4u4fe1u606f - u5982u679cu5b58u5728u5219u66f4u65b0uff0cu4e0du5b58u5728u5219u521bu5efa
     * @param mixRatio u914du6bd4u4fe1u606f
     * @return u540cu6b65u540eu7684u914du6bd4u5b9eu4f53
     */
    @Transactional
    public MixRatio syncMixRatio(MixRatio mixRatio) {
        if (mixRatio == null) return null;
        
        // u5148u6839u636eu5ba2u6237u7aefIDu67e5u627eu73b0u6709u914du6bd4
        Optional<MixRatio> existingRatio = null;
        
        // u5982u679cu6709IDuff0cu5148u6839u636eIDu67e5u627e
        if (mixRatio.getId() != null) {
            existingRatio = mixRatioRepository.findById(mixRatio.getId());
        }
        
        // u5982u679cu627eu4e0du5230uff0cu5219u6839u636eu540du79f0u548cu7ec4u7ec7IDu67e5u627e
        if (existingRatio == null || !existingRatio.isPresent()) {
            existingRatio = mixRatioRepository.findByNameAndOrganizationId(
                mixRatio.getName(), mixRatio.getOrganizationId());
        }
        
        if (existingRatio != null && existingRatio.isPresent()) {
            // u66f4u65b0u73b0u6709u914du6bd4
            MixRatio existing = existingRatio.get();
            existing.setName(mixRatio.getName());
            if (mixRatio.getDescription() != null) {
                existing.setDescription(mixRatio.getDescription());
            }
            // u5982u679cu6709u9879u76eeu5173u8054uff0cu4fddu6301u5173u8054
            if (mixRatio.getProject() != null) {
                existing.setProject(mixRatio.getProject());
            }
            // u5982u679cu6709u539fu6599u5173u8054uff0cu5219u540cu6b65u539fu6599
            if (mixRatio.getMaterials() != null && !mixRatio.getMaterials().isEmpty()) {
                // u540cu6b65u539fu6599u5173u8054uff0cu53efu80fdu9700u8981u5148u540cu6b65u539fu6599u5b9eu4f53
                existing.setMaterials(syncMaterials(mixRatio.getMaterials()));
            }
            return mixRatioRepository.save(existing);
        } else {
            // u521bu5efau65b0u914du6bd4
            // u5982u679cu6709u539fu6599u5173u8054uff0cu5148u540cu6b65u539fu6599
            if (mixRatio.getMaterials() != null && !mixRatio.getMaterials().isEmpty()) {
                mixRatio.setMaterials(syncMaterials(mixRatio.getMaterials()));
            }
            return mixRatioRepository.save(mixRatio);
        }
    }
    
    /**
     * u540cu6b65u539fu6599u4fe1u606f - u5982u679cu5b58u5728u5219u66f4u65b0uff0cu4e0du5b58u5728u5219u521bu5efa
     * @param materials u539fu6599u4fe1u606fu5217u8868
     * @return u540cu6b65u540eu7684u539fu6599u5b9eu4f53u5217u8868
     */
    @Transactional
    public List<Material> syncMaterials(List<Material> materials) {
        if (materials == null || materials.isEmpty()) return new ArrayList<>();
        
        List<Material> syncedMaterials = new ArrayList<>();
        
        for (Material material : materials) {
            // u5148u6839u636eu5ba2u6237u7aefIDu67e5u627eu73b0u6709u539fu6599
            Optional<Material> existingMaterial = null;
            
            // u5982u679cu6709IDuff0cu5148u6839u636eIDu67e5u627e
            if (material.getId() != null) {
                existingMaterial = materialRepository.findById(material.getId());
            }
            
            // u5982u679cu627eu4e0du5230uff0cu5219u6839u636eu540du79f0u548cu7ec4u7ec7IDu67e5u627e
            if ((existingMaterial == null || !existingMaterial.isPresent()) && material.getClientId() != null) {
                existingMaterial = materialRepository.findByClientId(material.getClientId());
            }
            
            // u5982u679cu627eu4e0du5230uff0cu5219u6839u636eu540du79f0u548cu7ec4u7ec7IDu67e5u627e
            if ((existingMaterial == null || !existingMaterial.isPresent()) && material.getName() != null) {
                existingMaterial = materialRepository.findByNameAndOrganizationId(
                    material.getName(), material.getOrganizationId());
            }
            
            if (existingMaterial != null && existingMaterial.isPresent()) {
                // u66f4u65b0u73b0u6709u539fu6599
                Material existing = existingMaterial.get();
                // u5982u679cu6709u540du79f0uff0cu4fddu6301u540du79f0
                if (material.getName() != null) {
                    existing.setName(material.getName());
                }
                if (material.getDescription() != null) {
                    existing.setDescription(material.getDescription());
                }
                if (material.getType() != null) {
                    existing.setType(material.getType());
                }
                syncedMaterials.add(materialRepository.save(existing));
            } else {
                // u5982u679cu6709u540du79f0uff0cu521bu5efau65b0u539fu6599
                if (material.getName() != null) {
                    // u4fddu6301u65b0u539fu6599u6709u7c7bu578bu4e3au4e00
                    if (material.getType() == null) {
                        material.setType("unknown");
                    }
                    syncedMaterials.add(materialRepository.save(material));
                } else {
                    // u8bb0u5f55u9519u8befu65e5u5fd7
                    log.warn("u6d88u606fu540cu6b65u539fu6599uff1au4e0du5b58u540du79f0u5b57u6bbfu3002u539fu6599IDuff1a" + material.getId());
                }
            }
        }
        
        return syncedMaterials;
    }

    /**
     * u4feeu6539u5b9eu9a8cu4efbu52a1u540cu6b65u65b9u6cd5uff0cu589eu5f3au9519u8befu5904u7406
     */
    @Transactional
    public ExperimentTask syncExperimentTask(ExperimentTask experimentTask) {
        try {
            Optional<ExperimentTask> existingByClientId = 
                experimentTaskRepository.findByClientId(experimentTask.getClientId());
            
            if (existingByClientId.isPresent()) {
                // u66f4u65b0u73b0u6709u5b9eu9a8cu4efbu52a1
                ExperimentTask existing = existingByClientId.get();
                
                // u66f4u65b0u57fau672cu4fe1u606f
                existing.setName(experimentTask.getName());
                if (experimentTask.getDescription() != null) {
                    existing.setDescription(experimentTask.getDescription());
                }
                
                // u66f4u65b0u65f6u95f4u548cu72b6u6001
                if (experimentTask.getScheduledStartTime() != null) {
                    existing.setScheduledStartTime(experimentTask.getScheduledStartTime());
                }
                if (experimentTask.getScheduledEndTime() != null) {
                    existing.setScheduledEndTime(experimentTask.getScheduledEndTime());
                }
                if (experimentTask.getActualStartTime() != null) {
                    existing.setActualStartTime(experimentTask.getActualStartTime());
                }
                if (experimentTask.getActualEndTime() != null) {
                    existing.setActualEndTime(experimentTask.getActualEndTime());
                }
                if (experimentTask.getStatus() != null) {
                    existing.setStatus(experimentTask.getStatus());
                }
                
                // u66f4u65b0u5173u8054
                if (experimentTask.getProject() != null) {
                    existing.setProject(syncProject(experimentTask.getProject()));
                }
                if (experimentTask.getMixingMethod() != null) {
                    existing.setMixingMethod(syncMixingMethod(experimentTask.getMixingMethod()));
                }
                if (experimentTask.getMixRatio() != null) {
                    existing.setMixRatio(syncMixRatio(experimentTask.getMixRatio()));
                }
                if (experimentTask.getMaterials() != null && !experimentTask.getMaterials().isEmpty()) {
                    existing.setMaterials(syncMaterials(experimentTask.getMaterials()));
                }
                
                // u8bbeu7f6eu5df2u540cu6b65u6807u8bb0
                existing.setSynced(true);
                existing.setSyncStatus("SYNCED");
                
                return experimentTaskRepository.save(existing);
            } else {
                // u521bu5efau65b0u5b9eu9a8cu4efbu52a1
                experimentTask.setSynced(true);
                experimentTask.setSyncStatus("SYNCED");
                return experimentTaskRepository.save(experimentTask);
            }
        } catch (Exception e) {
            // u8bb0u5f55u9519u8befu65e5u5fd7
            log.error("Error syncing experiment task: " + e.getMessage(), e);
            // u8bbeu7f6eu540cu6b65u72b6u6001u4e3au9519u8bef
            experimentTask.setSynced(false);
            experimentTask.setSyncStatus("ERROR: " + e.getMessage());
            throw e;
        }
    }
}
