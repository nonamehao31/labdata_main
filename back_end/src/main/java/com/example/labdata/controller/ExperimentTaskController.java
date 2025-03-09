package com.example.labdata.controller;

import com.example.labdata.model.ExperimentTask;
import com.example.labdata.model.Material;
import com.example.labdata.model.MixingMethod;
import com.example.labdata.model.MixRatio;
import com.example.labdata.model.Project;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.service.ExperimentTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
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

    /**
     * 同步实验任务
     * 这个接口不仅同步实验任务本身，还会处理相关的项目、配比、混合方法和原料
     */
    @PostMapping("/sync")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ExperimentTask> syncExperimentTask(@Valid @RequestBody ExperimentTask experimentTask) {
        // 获取当前用户信息，用于数据隔离
        UserPrincipal currentUser = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long organizationId = currentUser.getOrganizationId();
        
        // 设置组织ID以确保数据隔离
        if (experimentTask.getOrganizationId() == null) {
            experimentTask.setOrganizationId(organizationId);
        }
        
        // 处理项目依赖 - 如果项目信息完整则创建或更新项目
        if (experimentTask.getProject() != null && experimentTask.getProject().getName() != null) {
            Project project = experimentTaskService.syncProject(experimentTask.getProject());
            experimentTask.setProject(project);
        }
        
        // 处理混合方法依赖 - 如果混合方法信息完整则创建或更新
        if (experimentTask.getMixingMethod() != null && experimentTask.getMixingMethod().getName() != null) {
            MixingMethod mixingMethod = experimentTaskService.syncMixingMethod(experimentTask.getMixingMethod());
            experimentTask.setMixingMethod(mixingMethod);
        }
        
        // 处理配比依赖 - 如果配比信息完整则创建或更新
        if (experimentTask.getMixRatio() != null && experimentTask.getMixRatio().getName() != null) {
            MixRatio mixRatio = experimentTaskService.syncMixRatio(experimentTask.getMixRatio());
            experimentTask.setMixRatio(mixRatio);
        }
        
        // 处理原料依赖 - 如果原料信息完整则创建或更新
        if (experimentTask.getMaterials() != null && !experimentTask.getMaterials().isEmpty()) {
            List<Material> materials = experimentTaskService.syncMaterials(experimentTask.getMaterials());
            experimentTask.setMaterials(materials);
        }
        
        // 最后同步实验任务本身
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
