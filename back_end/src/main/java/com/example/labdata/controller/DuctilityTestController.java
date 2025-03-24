package com.example.labdata.controller;

import com.example.labdata.model.DuctilityTest;
import com.example.labdata.payload.request.DuctilityTestRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.DuctilityTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 延度实验数据控制器
 */
@RestController
@RequestMapping("/api/ductility")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DuctilityTestController {
    private static final Logger logger = LoggerFactory.getLogger(DuctilityTestController.class);

    @Autowired
    private DuctilityTestService ductilityTestService;

    /**
     * 提交延度实验数据
     * @param request 延度实验数据请求
     * @param currentUser 当前用户
     * @return API响应
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> submitDuctilityTest(
            @Valid @RequestBody DuctilityTestRequest request,
            @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("接收到延度实验数据提交请求，任务ID: {}, 用户: {}", 
                request.getTaskId(), currentUser.getUsername());
            
            // 数据验证
            if (request.getTaskId() == null || request.getTaskId().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "任务ID不能为空"));
            }
            
            if (request.getTemperature() == null || request.getTemperature().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "温度不能为空"));
            }
            
            if (request.getDisplacement() == null || request.getDisplacement().isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "拉长位移不能为空"));
            }
            
            // 保存数据
            DuctilityTest saved = ductilityTestService.saveDuctilityTestData(request);
            logger.info("延度实验数据保存成功，ID: {}", saved.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "延度实验数据保存成功"));
        } catch (Exception e) {
            logger.error("保存延度实验数据时出错: ", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "保存延度实验数据时出错: " + e.getMessage()));
        }
    }

    /**
     * 根据任务ID获取延度实验数据
     * @param taskId 任务ID
     * @param currentUser 当前用户
     * @return API响应
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DuctilityTest>> getDuctilityTestByTaskId(
            @PathVariable String taskId,
            @CurrentUser UserPrincipal currentUser) {
        try {
            logger.info("获取延度实验数据，任务ID: {}, 用户: {}", 
                taskId, currentUser.getUsername());
                
            var results = ductilityTestService.getDuctilityTestDataByTaskId(taskId);
            if (results.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到延度实验数据"));
            }
            
            // 返回最新的一条数据
            return ResponseEntity.ok(new ApiResponse<>(true, "获取延度实验数据成功", results.get(0)));
        } catch (Exception e) {
            logger.error("获取延度实验数据时出错: ", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取延度实验数据时出错: " + e.getMessage()));
        }
    }
}
