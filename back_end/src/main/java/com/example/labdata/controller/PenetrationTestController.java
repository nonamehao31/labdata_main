package com.example.labdata.controller;

import com.example.labdata.model.PenetrationTest;
import com.example.labdata.payload.request.PenetrationTestRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.PenetrationTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 针入度实验控制器
 * 处理针入度实验相关API请求
 */
@RestController
@RequestMapping("/api/asphalt")
public class PenetrationTestController {

    private static final Logger logger = LoggerFactory.getLogger(PenetrationTestController.class);

    @Autowired
    private PenetrationTestService penetrationTestService;

    /**
     * 提交针入度实验数据
     *
     * @param request 针入度实验数据请求
     * @param currentUser 当前用户
     * @return 是否提交成功
     */
    @PostMapping("/penetration")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> submitPenetrationTest(
            @Valid @RequestBody PenetrationTestRequest request,
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("用户 {} 提交针入度实验数据，任务ID: {}", currentUser.getUsername(), request.getTaskId());
        
        try {
            // 数据验证
            if (request.getTaskId() == null || request.getTaskId().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "任务ID不能为空", false));
            }
            
            if (request.getTemperature() == null || request.getTemperature().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "温度不能为空", false));
            }
            
            if (request.getReading() == null || request.getReading().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "读数不能为空", false));
            }
            
            // 提交数据
            boolean success = penetrationTestService.submitPenetrationTest(request);
            
            if (success) {
                return ResponseEntity.ok(new ApiResponse<>(true, "针入度实验数据提交成功", true));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "针入度实验数据提交失败", false));
            }
            
        } catch (Exception e) {
            logger.error("提交针入度实验数据时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ApiResponse<>(false, "提交针入度实验数据时发生错误: " + e.getMessage(), false));
        }
    }
    
    /**
     * 根据任务ID获取针入度实验数据
     *
     * @param taskId 任务ID
     * @param currentUser 当前用户
     * @return 针入度实验数据
     */
    @GetMapping("/penetration/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PenetrationTest>> getPenetrationTestByTaskId(
            @PathVariable String taskId,
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("用户 {} 获取针入度实验数据，任务ID: {}", currentUser.getUsername(), taskId);
        
        try {
            Optional<PenetrationTest> penetrationTest = penetrationTestService.getPenetrationTestByTaskId(taskId);
            
            if (penetrationTest.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "获取针入度实验数据成功", penetrationTest.get()));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到任务ID为 " + taskId + " 的针入度实验数据", null));
            }
            
        } catch (Exception e) {
            logger.error("获取针入度实验数据时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取针入度实验数据时发生错误: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取所有针入度实验数据
     *
     * @param currentUser 当前用户
     * @return 针入度实验数据列表
     */
    @GetMapping("/penetration")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<PenetrationTest>>> getAllPenetrationTests(
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("用户 {} 获取所有针入度实验数据", currentUser.getUsername());
        
        try {
            List<PenetrationTest> penetrationTests = penetrationTestService.getAllPenetrationTests();
            return ResponseEntity.ok(new ApiResponse<>(true, "获取所有针入度实验数据成功", penetrationTests));
        } catch (Exception e) {
            logger.error("获取所有针入度实验数据时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取所有针入度实验数据时发生错误: " + e.getMessage(), null));
        }
    }
}
