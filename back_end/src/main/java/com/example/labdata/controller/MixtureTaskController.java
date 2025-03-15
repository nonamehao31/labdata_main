package com.example.labdata.controller;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.service.MixtureTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mixtureTask")
public class MixtureTaskController {

    @Autowired
    private MixtureTaskService mixtureTaskService;

    @GetMapping("/list")
    public ResponseEntity<List<MixtureTask>> getAllMixtureTasks() {
        return ResponseEntity.ok(mixtureTaskService.getAllMixtureTasks());
    }
    
    @GetMapping("/listByType")
    public ResponseEntity<List<MixtureTask>> getMixtureTasksByType(@RequestParam String taskType) {
        return ResponseEntity.ok(mixtureTaskService.getMixtureTasksByType(taskType));
    }
}
