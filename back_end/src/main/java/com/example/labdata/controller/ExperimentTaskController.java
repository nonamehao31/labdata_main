package com.example.labdata.controller;

import com.example.labdata.model.ExperimentTask;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.service.ExperimentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/experiment-tasks")
public class ExperimentTaskController {

    @Autowired
    private ExperimentTaskService experimentTaskService;

    @GetMapping
    public List<ExperimentTask> getAllExperimentTasks() {
        return experimentTaskService.getAllExperimentTasks();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExperimentTask> getExperimentTaskById(@PathVariable Long id) {
        ExperimentTask experimentTask = experimentTaskService.getExperimentTaskById(id);
        return ResponseEntity.ok(experimentTask);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createExperimentTask(@Valid @RequestBody ExperimentTask experimentTask) {
        ExperimentTask savedExperimentTask = experimentTaskService.createExperimentTask(experimentTask);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedExperimentTask.getId()).toUri();

        return ResponseEntity.created(location)
                .body(savedExperimentTask);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExperimentTask> updateExperimentTask(
            @PathVariable Long id, @Valid @RequestBody ExperimentTask experimentTaskDetails) {
        ExperimentTask updatedExperimentTask = experimentTaskService.updateExperimentTask(id, experimentTaskDetails);
        return ResponseEntity.ok(updatedExperimentTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteExperimentTask(@PathVariable Long id) {
        experimentTaskService.deleteExperimentTask(id);
        return ResponseEntity.ok(new ApiResponse(true, "Experiment task deleted successfully"));
    }
    
    @GetMapping("/user/{userId}")
    public List<ExperimentTask> getExperimentTasksByUserId(@PathVariable Long userId) {
        return experimentTaskService.findExperimentTasksByUserId(userId);
    }
    
    @GetMapping("/specimen/{specimenId}")
    public List<ExperimentTask> getExperimentTasksBySpecimenId(@PathVariable Long specimenId) {
        return experimentTaskService.findExperimentTasksBySpecimenId(specimenId);
    }
    
    @GetMapping("/status/{status}")
    public List<ExperimentTask> getExperimentTasksByStatus(
            @PathVariable ExperimentTask.TaskStatus status) {
        return experimentTaskService.findExperimentTasksByStatus(status);
    }
    
    @GetMapping("/dateRange")
    public List<ExperimentTask> getExperimentTasksByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return experimentTaskService.findExperimentTasksByDateRange(start, end);
    }

    // 同步相关端点
    @GetMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public List<ExperimentTask> getExperimentTasksModifiedSince(@RequestParam("since") Instant since) {
        return experimentTaskService.findModifiedSince(since);
    }

    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExperimentTask> syncExperimentTask(@Valid @RequestBody ExperimentTask experimentTask) {
        ExperimentTask syncedExperimentTask = experimentTaskService.syncExperimentTask(experimentTask);
        return ResponseEntity.ok(syncedExperimentTask);
    }

    @PostMapping("/sync/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> syncExperimentTasks(@Valid @RequestBody List<ExperimentTask> experimentTasks) {
        List<ExperimentTask> syncedExperimentTasks = experimentTasks.stream()
                .map(experimentTaskService::syncExperimentTask)
                .toList();
        return ResponseEntity.ok(syncedExperimentTasks);
    }
}

