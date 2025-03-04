package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.ExperimentTask;
import com.example.labdata.repository.ExperimentTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ExperimentTaskService {
    
    @Autowired
    private ExperimentTaskRepository experimentTaskRepository;
    
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
    
    @Transactional
    public ExperimentTask syncExperimentTask(ExperimentTask experimentTask) {
        // 检查是否已存在对应的实验任务
        Optional<ExperimentTask> existingByClientId = experimentTaskRepository.findByClientId(experimentTask.getClientId());
        
        if (existingByClientId.isPresent()) {
            // 更新现有实验任务
            ExperimentTask existing = existingByClientId.get();
            existing.setName(experimentTask.getName());
            existing.setDescription(experimentTask.getDescription());
            existing.setExperimentType(experimentTask.getExperimentType());
            existing.setSpecimen(experimentTask.getSpecimen());
            existing.setAssignedTo(experimentTask.getAssignedTo());
            existing.setScheduledStartTime(experimentTask.getScheduledStartTime());
            existing.setScheduledEndTime(experimentTask.getScheduledEndTime());
            existing.setActualStartTime(experimentTask.getActualStartTime());
            existing.setActualEndTime(experimentTask.getActualEndTime());
            existing.setStatus(experimentTask.getStatus());
            existing.setSynced(true);
            existing.setSyncStatus("SYNCED");
            return experimentTaskRepository.save(existing);
        } else {
            // 创建新实验任务
            experimentTask.setSynced(true);
            experimentTask.setSyncStatus("SYNCED");
            return experimentTaskRepository.save(experimentTask);
        }
    }
}
