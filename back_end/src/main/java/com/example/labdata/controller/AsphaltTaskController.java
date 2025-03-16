package com.example.labdata.controller;

import com.example.labdata.model.AsphaltTask;
import com.example.labdata.payload.request.AsphaltExperimentRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.AsphaltExperimentResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.AsphaltTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 沥青实验任务控制器
 * 提供沥青实验任务相关API
 */
@RestController
@RequestMapping("/api/asphalt/experiments")
public class AsphaltTaskController {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltTaskController.class);

    @Autowired
    private AsphaltTaskService asphaltTaskService;

    /**
     * 创建单个沥青实验任务
     *
     * @param request     沥青实验请求
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AsphaltExperimentResponse>> createAsphaltExperiment(
            @Valid @RequestBody AsphaltExperimentRequest request,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 创建沥青实验: {}", currentUser.getUsername(), request.getAsphaltExperimentName());

        AsphaltTask asphaltTask = asphaltTaskService.createAsphaltExperiment(request);
        AsphaltExperimentResponse response = new AsphaltExperimentResponse(asphaltTask);

        return ResponseEntity.ok(new ApiResponse<>(true, "沥青实验创建成功", response));
    }

    /**
     * 批量创建沥青实验任务
     *
     * @param requests    沥青实验请求列表
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AsphaltExperimentResponse>>> createAsphaltExperiments(
            @Valid @RequestBody List<AsphaltExperimentRequest> requests,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 批量创建沥青实验: {} 条", currentUser.getUsername(), requests.size());
        // 添加详细日志，显示请求内容
        for (AsphaltExperimentRequest req : requests) {
            logger.info("收到请求: 实验名称={}, 实验类型={}, 任务名称={}",
                    req.getAsphaltExperimentName(),
                    req.getAsphaltExperimentType(),
                    req.getAsphaltTaskName());
        }

        List<AsphaltTask> asphaltTasks = asphaltTaskService.createAsphaltExperiments(requests);
        List<AsphaltExperimentResponse> responses = asphaltTasks.stream()
                .map(AsphaltExperimentResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "沥青实验批量创建成功", responses));
    }

    /**
     * 获取所有沥青实验任务
     *
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AsphaltExperimentResponse>>> getAllAsphaltExperiments(
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取所有沥青实验", currentUser.getUsername());

        List<AsphaltTask> asphaltTasks = asphaltTaskService.getAllAsphaltExperiments();
        List<AsphaltExperimentResponse> responses = asphaltTasks.stream()
                .map(AsphaltExperimentResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青实验列表成功", responses));
    }

    /**
     * 根据ID获取沥青实验任务
     *
     * @param id          沥青实验任务ID
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AsphaltExperimentResponse>> getAsphaltExperimentById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取沥青实验 ID: {}", currentUser.getUsername(), id);

        AsphaltTask asphaltTask = asphaltTaskService.getAsphaltExperimentById(id);
        
        if (asphaltTask == null) {
            return ResponseEntity.ok(new ApiResponse<>(false, "未找到ID为 " + id + " 的沥青实验", null));
        }
        
        AsphaltExperimentResponse response = new AsphaltExperimentResponse(asphaltTask);
        return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青实验成功", response));
    }

    /**
     * 根据类型获取沥青实验任务
     *
     * @param type        沥青实验任务类型
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AsphaltExperimentResponse>>> getAsphaltExperimentsByType(
            @PathVariable String type,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取类型为 {} 的沥青实验", currentUser.getUsername(), type);

        List<AsphaltTask> asphaltTasks = asphaltTaskService.getAsphaltExperimentsByType(type);
        List<AsphaltExperimentResponse> responses = asphaltTasks.stream()
                .map(AsphaltExperimentResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(true, "获取类型为 " + type + " 的沥青实验列表成功", responses));
    }

    /**
     * 获取所有沥青实验任务（按公司过滤）
     *
     * @param companyId   公司ID
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/byCompany")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AsphaltExperimentResponse>>> getAsphaltExperimentsByCompany(
            @RequestParam String companyId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取沥青实验任务，公司ID: {}", currentUser.getUsername(), companyId);

        List<AsphaltTask> asphaltTasks = asphaltTaskService.getAllAsphaltExperiments();
        // 过滤出该公司的沥青实验任务
        List<AsphaltTask> companyTasks = asphaltTasks.stream()
                .filter(task -> task.getCompanyId() != null && task.getCompanyId().equals(companyId))
                .collect(Collectors.toList());
        
        // 添加调试日志，输出获取到的任务及其字段
        logger.info("获取到 {} 个公司任务", companyTasks.size());
        for (AsphaltTask task : companyTasks) {
            logger.info("任务ID: {}, 名称: {}, 任务名称: {}, 状态: {}, 任务状态: {}", 
                   task.getAsphaltExperimentId(), 
                   task.getAsphaltExperimentName(),
                   task.getAsphaltTaskName(),
                   task.getStatus(),
                   task.getTaskStatus());
        }
                
        List<AsphaltExperimentResponse> responses = companyTasks.stream()
                .map(AsphaltExperimentResponse::new)
                .collect(Collectors.toList());
        
        // 添加调试日志，输出响应对象及其字段
        logger.info("生成 {} 个响应对象", responses.size());
        for (AsphaltExperimentResponse response : responses) {
            logger.info("响应ID: {}, 名称: {}, 任务名称: {}, 状态: {}, 任务状态: {}", 
                   response.getAsphaltExperimentId(), 
                   response.getAsphaltExperimentName(),
                   response.getAsphaltTaskName(),
                   response.getStatus(),
                   response.getTaskStatus());
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "获取公司沥青实验列表成功", responses));
    }
}
