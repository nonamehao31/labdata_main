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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MixtureTaskController {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskController.class);

    @Autowired
    private MixtureTaskService mixtureTaskService;

    @GetMapping("/mixtureTask/list")
    public ResponseEntity<List<MixtureTask>> getAllMixtureTasks() {
        return ResponseEntity.ok(mixtureTaskService.getAllMixtureTasks());
    }
    
    @GetMapping("/mixtureTask/listByType")
    public ResponseEntity<List<MixtureTask>> getMixtureTasksByType(@RequestParam String taskType) {
        return ResponseEntity.ok(mixtureTaskService.getMixtureTasksByType(taskType));
    }
    
    /**
     * 根据任务ID获取项目名称
     * 
     * @param taskId 任务ID
     * @return 项目名称响应
     */
    @GetMapping("/mixtureTask/{taskId}/projectName")
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
    @GetMapping("/mixtureTask/{taskId}/pairs")
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
    @GetMapping("/mixtureTask/mixratio-details/{taskId}")
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
    @GetMapping("/mixtureTask/task-assignments/{taskId}")
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
    @GetMapping("/mixtureTask/task-remarks/{taskId}")
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

    /**
     * 根据任务ID和设备类型保存设备信息
     * 
     * @param taskId 任务ID
     * @param deviceType 设备类型 (MIXING, FORMING, TESTING)
     * @param deviceModel 设备型号
     * @param manufacturer 设备厂家（可选）
     * @return 设备信息和类型
     */
    @PutMapping("/mixtureTask/{taskId}/device")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveDeviceInfo(
            @PathVariable String taskId,
            @RequestParam String deviceType,
            @RequestParam String deviceModel,
            @RequestParam(required = false) String manufacturer) {
        logger.info("接收到设备信息保存请求，任务ID: {}, 设备类型: {}, 设备型号: {}, 厂家: {}", 
                     taskId, deviceType, deviceModel, manufacturer);
        try {
            Map<String, String> result = mixtureTaskService.saveDeviceInfo(taskId, deviceType, deviceModel, manufacturer);
            return ResponseEntity.ok(new ApiResponse<>(true, "设备信息保存成功", result));
        } catch (Exception e) {
            logger.error("保存设备信息时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "保存设备信息失败: " + e.getMessage(), null));
        }
    }

    /**
     * 根据任务ID前缀获取试件制备所需的方法、配比和设备信息
     * 
     * @param taskId 任务ID前缀
     * @return 制件方法、设备信息的响应
     */
    @GetMapping("/mixture-tasks/{taskId}/specimen-data")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSpecimenData(@PathVariable String taskId) {
        logger.info("接收到获取任务ID前缀: {} 的试件制备数据请求", taskId);
        try {
            Map<String, Object> specimenData = mixtureTaskService.getSpecimenData(taskId);
            
            if (specimenData.isEmpty() || 
                (specimenData.containsKey("methodsAndRatios") && ((List<?>)specimenData.get("methodsAndRatios")).isEmpty())) {
                logger.warn("未找到任务ID前缀: {} 的试件数据", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "未找到相关试件数据", new HashMap<>()));
            }
            
            logger.info("成功获取任务ID前缀: {} 的试件制备数据", taskId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取试件数据成功", specimenData));
        } catch (Exception e) {
            logger.error("获取试件制备数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取试件数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 更新任务ID前缀对应的所有任务making_status为finished
     * 
     * @param taskIdPrefix 任务ID前缀
     * @return 更新结果的响应
     */
    @PutMapping("/mixture-tasks/{taskIdPrefix}/making_status/finished")
    public ResponseEntity<ApiResponse<Boolean>> updateMakingStatusToFinished(@PathVariable String taskIdPrefix) {
        logger.info("接收到更新制件状态请求，任务ID前缀: {}", taskIdPrefix);
        try {
            boolean result = mixtureTaskService.updateMakingStatusToFinished(taskIdPrefix);
            if (result) {
                logger.info("成功更新任务ID前缀={}的所有任务制件状态为'已完成'", taskIdPrefix);
                return ResponseEntity.ok(new ApiResponse<>(true, "更新制件状态成功", true));
            } else {
                logger.warn("未找到任务ID前缀={}的任务，或更新失败", taskIdPrefix);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到任务或更新失败", false));
            }
        } catch (Exception e) {
            logger.error("更新制件状态时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "更新制件状态失败: " + e.getMessage(), false));
        }
    }
}
