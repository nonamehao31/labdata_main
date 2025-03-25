package com.example.labdata.controller;

import com.example.labdata.model.AsphaltTask;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.AsphaltTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 沥青实验任务更新控制器
 * 提供沥青实验任务状态更新相关API
 * 匹配前端路径 /api/asphalt-task
 */
@RestController
@RequestMapping("/api/asphalt-task")
public class AsphaltTaskUpdateController {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltTaskUpdateController.class);

    @Autowired
    private AsphaltTaskService asphaltTaskService;

    /**
     * 更新特定实验类型的状态为已完成（匹配前端调用路径）
     *
     * @param taskId 任务ID（查询参数）
     * @param experimentType 实验类型（查询参数）
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/update-experiment-type-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> updateExperimentTypeStatusToFinished(
            @RequestParam String taskId,
            @RequestParam String experimentType,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 通过新接口更新实验任务状态为已完成, 任务ID: {}, 实验类型: {}", 
            currentUser.getUsername(), taskId, experimentType);
        
        try {
            // 使用支持字符串ID的方法，避免Long类型转换错误
            AsphaltTask updatedTask = asphaltTaskService.updateExperimentStatusToFinishedByStringId(taskId, experimentType);
            
            if (updatedTask != null) {
                logger.info("成功更新实验任务状态为已完成: {}", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "实验任务状态更新成功", true));
            } else {
                logger.warn("更新实验任务状态失败，未找到匹配的任务: {}", taskId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务", false));
            }
        } catch (Exception e) {
            logger.error("更新实验任务状态失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "更新实验任务状态失败: " + e.getMessage(), false));
        }
    }
    
    /**
     * 获取实验任务状态
     *
     * @param taskId 任务ID
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/experimentStatus/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> getExperimentStatus(
            @PathVariable String taskId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取实验任务状态, 任务ID: {}", currentUser.getUsername(), taskId);
        
        try {
            // 使用支持字符串ID的方法
            String status = asphaltTaskService.getExperimentStatusByStringId(taskId);
            
            if (status != null) {
                logger.info("获取实验任务状态成功: taskId={}, status={}", taskId, status);
                return ResponseEntity.ok(new ApiResponse<>(true, "获取实验任务状态成功", status));
            } else {
                logger.warn("获取实验任务状态失败，未找到匹配的任务: {}", taskId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务", null));
            }
        } catch (Exception e) {
            logger.error("获取实验任务状态失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取实验任务状态失败: " + e.getMessage(), null));
        }
    }
}
