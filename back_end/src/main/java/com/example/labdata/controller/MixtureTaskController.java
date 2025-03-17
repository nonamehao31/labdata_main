package com.example.labdata.controller;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.MixratioSpecimenPairResponse;
import com.example.labdata.payload.response.MixRatioDetailResponse;
import com.example.labdata.payload.response.ProjectNameResponse;
import com.example.labdata.service.MixtureTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mixtureTask")
public class MixtureTaskController {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskController.class);

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
    
    /**
     * 根据任务ID获取项目名称
     * 
     * @param taskId 任务ID
     * @return 项目名称响应
     */
    @GetMapping("/{taskId}/projectName")
    public ResponseEntity<ProjectNameResponse> getProjectNameByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务ID: {} 的项目名称请求", taskId);
        String projectName = mixtureTaskService.getProjectNameByTaskId(taskId);
        logger.info("返回项目名称: {}", projectName);
        return ResponseEntity.ok(new ProjectNameResponse(projectName));
    }

    /**
     * 根据任务ID获取配比和制件组合
     * 
     * @param taskId 任务ID
     * @return 配比和制件组合响应
     */
    @GetMapping("/{taskId}/pairs")
    public ResponseEntity<ApiResponse<List<MixratioSpecimenPairResponse>>> getMixratioSpecimenPairsByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务ID: {} 的配比和制件组合请求", taskId);
        List<MixratioSpecimenPairResponse> pairs = mixtureTaskService.getMixratioSpecimenPairsByTaskId(taskId);
        logger.info("返回任务ID: {} 的配比和制件组合，共 {} 个组合", taskId, pairs.size());
        return ResponseEntity.ok(new ApiResponse<List<MixratioSpecimenPairResponse>>(true, "获取成功", pairs));
    }

    /**
     * 根据任务ID获取配比详细信息列表
     * 
     * @param taskId 任务ID
     * @return 配比详细信息列表
     */
    @GetMapping("/mixratio-details/{taskId}")
    public ResponseEntity<ApiResponse<List<MixRatioDetailResponse>>> getMixRatioDetailsByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务配比详情请求，任务ID: {}", taskId);
        try {
            List<MixRatioDetailResponse> details = mixtureTaskService.getMixRatioDetailsByTaskId(taskId);
            if (details.isEmpty()) {
                logger.warn("未找到任务ID={}的配比详情", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "未找到配比详情", null));
            }
            logger.info("成功获取任务ID={}的配比详情，共{}条", taskId, details.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取配比详情成功", details));
        } catch (Exception e) {
            logger.error("获取任务配比详情时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取配比详情失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据任务ID获取实验指派信息
     * 
     * @param taskId 任务数据库主键ID（不是task_id字符串）
     * @return 配比ID到实验指派的映射
     */
    @GetMapping("/task-assignments/{taskId}")
    public ResponseEntity<ApiResponse<Map<Long, List<String>>>> getTaskAssignmentsByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务实验指派请求，任务ID: {}", taskId);
        try {
            Map<Long, List<String>> assignments = mixtureTaskService.getTaskAssignmentsByTaskId(taskId);
            if (assignments.isEmpty()) {
                logger.warn("未找到任务ID={}的实验指派信息", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "未找到实验指派信息", new HashMap<>()));
            }
            logger.info("成功获取任务ID={}的实验指派信息，共{}个配比有实验指派", taskId, assignments.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取实验指派信息成功", assignments));
        } catch (Exception e) {
            logger.error("获取任务实验指派信息时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取实验指派信息失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据任务ID获取备注信息
     * 
     * @param taskId 任务数据库主键ID（不是task_id字符串）
     * @return 备注信息
     */
    @GetMapping("/task-remarks/{taskId}")
    public ResponseEntity<ApiResponse<String>> getTaskRemarksByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务备注请求，任务ID: {}", taskId);
        try {
            String remarks = mixtureTaskService.getTaskRemarksByTaskId(taskId);
            logger.info("成功获取任务ID={}的备注信息: {}", taskId, remarks);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取备注信息成功", remarks));
        } catch (Exception e) {
            logger.error("获取任务备注信息时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取备注信息失败: " + e.getMessage(), null));
        }
    }
}
