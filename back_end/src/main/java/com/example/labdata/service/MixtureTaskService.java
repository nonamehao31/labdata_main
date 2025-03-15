package com.example.labdata.service;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.repository.MixtureTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MixtureTaskService {

    @Autowired
    private MixtureTaskRepository mixtureTaskRepository;

    public List<MixtureTask> getAllMixtureTasks() {
        return mixtureTaskRepository.findAll();
    }
    
    public List<MixtureTask> getMixtureTasksByType(String taskType) {
        return mixtureTaskRepository.findByTaskType(taskType);
    }
}
