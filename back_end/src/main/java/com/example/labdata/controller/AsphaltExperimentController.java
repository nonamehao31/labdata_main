package com.example.labdata.controller;

import com.example.labdata.model.AsphaltExperimentAssignment;
import com.example.labdata.model.AsphaltInfo;
import com.example.labdata.model.ExperimentTask;
import com.example.labdata.model.ExperimentType;
import com.example.labdata.payload.ApiResponse;
import com.example.labdata.payload.asphalt.AsphaltExperimentRequest;
import com.example.labdata.payload.asphalt.AsphaltExperimentResponse;
import com.example.labdata.repository.AsphaltExperimentAssignmentRepository;
import com.example.labdata.repository.AsphaltInfoRepository;
import com.example.labdata.repository.ExperimentTaskRepository;
import com.example.labdata.repository.ExperimentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/asphalt")
public class AsphaltExperimentController {

    @Autowired
    private AsphaltInfoRepository asphaltInfoRepository;

    @Autowired
    private AsphaltExperimentAssignmentRepository assignmentRepository;

    @Autowired
    private ExperimentTaskRepository experimentTaskRepository;

    @Autowired
    private ExperimentTypeRepository experimentTypeRepository;

    /**
     * 创建沥青实验任务
     */
    @PostMapping("/tasks")
    public ResponseEntity<?> createAsphaltExperimentTask(
            @RequestBody AsphaltExperimentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 创建实验任务
        ExperimentTask task = new ExperimentTask();
        task.setName(request.getTaskName());
        task.setDescription(request.getDescription());
        task.setStatus(ExperimentTask.TaskStatus.SCHEDULED);
        task.setCategory(ExperimentTask.ExperimentCategory.ASPHALT);
        task.setClientId(request.getClientId());
        task = experimentTaskRepository.save(task);
        
        // 存储沥青信息和实验分配
        List<AsphaltInfo> savedAsphaltInfos = new ArrayList<>();
        
        for (AsphaltExperimentRequest.AsphaltData asphaltData : request.getAsphaltDataList()) {
            // 创建沥青信息
            AsphaltInfo asphaltInfo = new AsphaltInfo();
            asphaltInfo.setGrade(asphaltData.getGrade());
            asphaltInfo.setType(asphaltData.getType());
            asphaltInfo.setSupplier(asphaltData.getSupplier());
            asphaltInfo.setClientId(asphaltData.getClientId());
            
            // 解析过期日期
            if (asphaltData.getExpiryDate() != null && !asphaltData.getExpiryDate().isEmpty()) {
                LocalDate expiryDate = LocalDate.parse(asphaltData.getExpiryDate(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                asphaltInfo.setExpiryDate(expiryDate);
            }
            
            // 设置关联的实验任务
            asphaltInfo.setExperimentTask(task);
            AsphaltInfo savedAsphaltInfo = asphaltInfoRepository.save(asphaltInfo);
            savedAsphaltInfos.add(savedAsphaltInfo);
            
            // 创建实验分配
            for (Long experimentTypeId : asphaltData.getExperimentTypeIds()) {
                ExperimentType experimentType = experimentTypeRepository.findById(experimentTypeId)
                        .orElse(null);
                if (experimentType != null) {
                    AsphaltExperimentAssignment assignment = new AsphaltExperimentAssignment();
                    assignment.setAsphaltInfo(savedAsphaltInfo);
                    assignment.setExperimentType(experimentType);
                    assignment.setExperimentTask(task);
                    assignment.setClientId(asphaltData.getAssignmentClientIds().get(
                        asphaltData.getExperimentTypeIds().indexOf(experimentTypeId)));
                    assignmentRepository.save(assignment);
                }
            }
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "沥青实验任务创建成功", task.getId()));
    }

    /**
     * 获取沥青实验任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<?> getAsphaltExperimentTask(@PathVariable Long taskId) {
        ExperimentTask task = experimentTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<AsphaltInfo> asphaltInfos = asphaltInfoRepository.findByExperimentTaskId(taskId);
        if (asphaltInfos.isEmpty()) {
            return ResponseEntity.ok(new AsphaltExperimentResponse(task, new ArrayList<>()));
        }
        
        // 构建完整的响应对象
        List<AsphaltExperimentResponse.AsphaltData> asphaltDataList = asphaltInfos.stream()
                .map(asphaltInfo -> {
                    // 获取关联的实验分配
                    List<AsphaltExperimentAssignment> assignments = 
                            assignmentRepository.findByAsphaltInfoId(asphaltInfo.getId());
                    
                    List<Long> experimentTypeIds = assignments.stream()
                            .map(assignment -> assignment.getExperimentType().getId())
                            .collect(Collectors.toList());
                    
                    List<Long> assignmentIds = assignments.stream()
                            .map(AsphaltExperimentAssignment::getId)
                            .collect(Collectors.toList());
                    
                    List<Long> assignmentClientIds = assignments.stream()
                            .map(AsphaltExperimentAssignment::getClientId)
                            .collect(Collectors.toList());
                    
                    // 构建沥青数据对象
                    AsphaltExperimentResponse.AsphaltData asphaltData = 
                            new AsphaltExperimentResponse.AsphaltData();
                    asphaltData.setId(asphaltInfo.getId());
                    asphaltData.setClientId(asphaltInfo.getClientId());
                    asphaltData.setGrade(asphaltInfo.getGrade());
                    asphaltData.setType(asphaltInfo.getType());
                    asphaltData.setSupplier(asphaltInfo.getSupplier());
                    asphaltData.setExpiryDate(asphaltInfo.getExpiryDate() != null ? 
                            asphaltInfo.getExpiryDate().toString() : null);
                    asphaltData.setExperimentTypeIds(experimentTypeIds);
                    asphaltData.setAssignmentIds(assignmentIds);
                    asphaltData.setAssignmentClientIds(assignmentClientIds);
                    
                    return asphaltData;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(new AsphaltExperimentResponse(task, asphaltDataList));
    }

    /**
     * 获取用户的所有沥青实验任务
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getAllAsphaltExperimentTasks() {
        List<ExperimentTask> tasks = experimentTaskRepository.findByCategory(
                ExperimentTask.ExperimentCategory.ASPHALT);
        
        return ResponseEntity.ok(tasks);
    }

    /**
     * 更新沥青实验任务
     */
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<?> updateAsphaltExperimentTask(
            @PathVariable Long taskId, 
            @RequestBody AsphaltExperimentRequest request) {
        ExperimentTask task = experimentTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 更新任务基本信息
        task.setName(request.getTaskName());
        task.setDescription(request.getDescription());
        experimentTaskRepository.save(task);
        
        // 处理沥青信息更新
        for (AsphaltExperimentRequest.AsphaltData asphaltData : request.getAsphaltDataList()) {
            if (asphaltData.getId() != null) {
                // 更新现有沥青信息
                AsphaltInfo asphaltInfo = asphaltInfoRepository.findById(asphaltData.getId()).orElse(null);
                if (asphaltInfo != null) {
                    asphaltInfo.setGrade(asphaltData.getGrade());
                    asphaltInfo.setType(asphaltData.getType());
                    asphaltInfo.setSupplier(asphaltData.getSupplier());
                    
                    if (asphaltData.getExpiryDate() != null && !asphaltData.getExpiryDate().isEmpty()) {
                        LocalDate expiryDate = LocalDate.parse(asphaltData.getExpiryDate(), 
                                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        asphaltInfo.setExpiryDate(expiryDate);
                    }
                    
                    asphaltInfoRepository.save(asphaltInfo);
                    
                    // 更新实验分配
                    updateExperimentAssignments(asphaltInfo, asphaltData, task);
                }
            } else {
                // 添加新的沥青信息
                AsphaltInfo newAsphaltInfo = new AsphaltInfo();
                newAsphaltInfo.setGrade(asphaltData.getGrade());
                newAsphaltInfo.setType(asphaltData.getType());
                newAsphaltInfo.setSupplier(asphaltData.getSupplier());
                newAsphaltInfo.setClientId(asphaltData.getClientId());
                
                if (asphaltData.getExpiryDate() != null && !asphaltData.getExpiryDate().isEmpty()) {
                    LocalDate expiryDate = LocalDate.parse(asphaltData.getExpiryDate(), 
                            DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    newAsphaltInfo.setExpiryDate(expiryDate);
                }
                
                newAsphaltInfo.setExperimentTask(task);
                AsphaltInfo savedAsphaltInfo = asphaltInfoRepository.save(newAsphaltInfo);
                
                // 添加实验分配
                for (Long experimentTypeId : asphaltData.getExperimentTypeIds()) {
                    ExperimentType experimentType = experimentTypeRepository.findById(experimentTypeId)
                            .orElse(null);
                    if (experimentType != null) {
                        AsphaltExperimentAssignment assignment = new AsphaltExperimentAssignment();
                        assignment.setAsphaltInfo(savedAsphaltInfo);
                        assignment.setExperimentType(experimentType);
                        assignment.setExperimentTask(task);
                        assignment.setClientId(asphaltData.getAssignmentClientIds().get(
                            asphaltData.getExperimentTypeIds().indexOf(experimentTypeId)));
                        assignmentRepository.save(assignment);
                    }
                }
            }
        }
        
        return ResponseEntity.ok(new ApiResponse(true, "沥青实验任务更新成功"));
    }
    
    /**
     * 辅助方法：更新实验分配
     */
    private void updateExperimentAssignments(
            AsphaltInfo asphaltInfo, 
            AsphaltExperimentRequest.AsphaltData asphaltData,
            ExperimentTask task) {
        // 获取当前所有分配
        List<AsphaltExperimentAssignment> currentAssignments = 
                assignmentRepository.findByAsphaltInfoId(asphaltInfo.getId());
        
        // 移除不再需要的分配
        List<Long> requestedTypeIds = asphaltData.getExperimentTypeIds();
        for (AsphaltExperimentAssignment assignment : currentAssignments) {
            if (!requestedTypeIds.contains(assignment.getExperimentType().getId())) {
                assignmentRepository.delete(assignment);
            }
        }
        
        // 查找现有分配中的实验类型ID
        List<Long> existingTypeIds = currentAssignments.stream()
                .map(a -> a.getExperimentType().getId())
                .collect(Collectors.toList());
        
        // 添加新的分配
        for (Long typeId : requestedTypeIds) {
            if (!existingTypeIds.contains(typeId)) {
                ExperimentType experimentType = experimentTypeRepository.findById(typeId).orElse(null);
                if (experimentType != null) {
                    AsphaltExperimentAssignment assignment = new AsphaltExperimentAssignment();
                    assignment.setAsphaltInfo(asphaltInfo);
                    assignment.setExperimentType(experimentType);
                    assignment.setExperimentTask(task);
                    assignment.setClientId(asphaltData.getAssignmentClientIds().get(
                        asphaltData.getExperimentTypeIds().indexOf(typeId)));
                    assignmentRepository.save(assignment);
                }
            }
        }
    }
}
