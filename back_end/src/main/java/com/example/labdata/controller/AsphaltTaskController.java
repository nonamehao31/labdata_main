package com.example.labdata.controller;

import com.example.labdata.model.AsphaltTask;
import com.example.labdata.payload.request.AsphaltExperimentRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.AsphaltDetailResponse;
import com.example.labdata.payload.response.AsphaltExperimentResponse;
import com.example.labdata.payload.response.AsphaltTaskAssignmentResponse;
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
     * 获取指定公司的沥青实验任务
     *
     * @param companyId   公司ID
     * @param username    用户名(可选)，如提供则只返回未接受任务和该用户接受的任务
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/byCompany")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<AsphaltExperimentResponse>>> getAsphaltExperimentsByCompany(
            @RequestParam String companyId,
            @RequestParam(required = false) String username,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取沥青实验任务，公司ID: {}, 过滤用户名: {}", currentUser.getUsername(), companyId, username);

        try {
            List<AsphaltTask> asphaltTasks = asphaltTaskService.getAllAsphaltExperiments();
            // 过滤出该公司的沥青实验任务
            List<AsphaltTask> filteredTasks = asphaltTasks.stream()
                    .filter(task -> task.getCompanyId() != null && task.getCompanyId().equals(companyId))
                    .filter(task -> {
                        // 如果提供了用户名，则只返回未接受的任务和该用户接受的任务
                        if (username != null && !username.isEmpty()) {
                            return !task.getTaskStatus().equals("ONGOING") || 
                                  (task.getAcceptor() != null && task.getAcceptor().equals(username));
                        }
                        // 否则返回所有任务
                        return true;
                    })
                    .collect(Collectors.toList());
            
            // 添加调试日志，输出筛选后的任务
            logger.info("筛选后获取到 {} 个公司任务", filteredTasks.size());
            for (AsphaltTask task : filteredTasks) {
                logger.info("任务ID: {}, 名称: {}, 任务名称: {}, 状态: {}, 任务状态: {}, 接受者: {}", 
                       task.getAsphaltExperimentId(), 
                       task.getAsphaltExperimentName(),
                       task.getAsphaltTaskName(),
                       task.getStatus(),
                       task.getTaskStatus(),
                       task.getAcceptor());
            }
                    
            List<AsphaltExperimentResponse> responses = filteredTasks.stream()
                    .map(AsphaltExperimentResponse::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new ApiResponse<>(true, "获取公司沥青实验列表成功", responses));
        } catch (Exception e) {
            logger.error("获取公司沥青实验列表失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取公司沥青实验列表失败", null));
        }
    }

    /**
     * 根据任务ID获取沥青任务详情信息
     *
     * @param asphaltExperimentId 任务ID
     * @return 包含沥青信息和实验指派信息的响应
     */
    @GetMapping("/detail/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AsphaltDetailResponse>> getAsphaltDetailByTaskId(@PathVariable String asphaltExperimentId) {
        logger.info("获取任务ID为{}的沥青任务详情", asphaltExperimentId);
        
        AsphaltDetailResponse detailResponse = asphaltTaskService.getAsphaltDetailByTaskId(asphaltExperimentId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青任务详情成功", detailResponse));
    }

    /**
     * 接受沥青实验任务
     *
     * @param asphaltExperimentId 任务ID
     * @param acceptor    接受者
     * @param acceptTime  接受时间
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/accept/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> acceptAsphaltTask(
            @PathVariable String asphaltExperimentId,
            @RequestParam String acceptor,
            @RequestParam Long acceptTime,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 接受沥青任务 ID: {}", currentUser.getUsername(), asphaltExperimentId);
        
        try {
            asphaltTaskService.acceptAsphaltTask(asphaltExperimentId, acceptor, acceptTime);
            return ResponseEntity.ok(new ApiResponse<>(true, "接受沥青任务成功", true));
        } catch (Exception e) {
            logger.error("接受沥青任务失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "接受沥青任务失败: " + e.getMessage(), false));
        }
    }

    /**
     * 更新实验任务状态为已完成
     *
     * @param asphaltExperimentId 任务ID
     * @param experimentType 实验类型
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/updateExperimentStatus/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> updateExperimentStatus(
            @PathVariable String asphaltExperimentId,
            @RequestParam String experimentType,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 更新实验任务状态为已完成, 任务ID: {}, 实验类型: {}", 
            currentUser.getUsername(), asphaltExperimentId, experimentType);
        
        try {
            // 使用支持字符串ID的方法，避免Long类型转换错误
            AsphaltTask updatedTask = asphaltTaskService.updateExperimentStatusToFinishedByStringId(asphaltExperimentId, experimentType);
            
            if (updatedTask != null) {
                logger.info("成功更新实验任务状态为已完成: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(true, "实验任务状态更新成功", true));
            } else {
                logger.warn("更新实验任务状态失败，未找到匹配的任务: {}", asphaltExperimentId);
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
     * @param asphaltExperimentId 任务ID
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/experimentStatus/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> getExperimentStatus(
            @PathVariable String asphaltExperimentId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取实验任务状态, 任务ID: {}", currentUser.getUsername(), asphaltExperimentId);
        
        try {
            // 使用支持字符串ID的方法
            String status = asphaltTaskService.getExperimentStatusByStringId(asphaltExperimentId);
            
            if (status != null) {
                logger.info("获取实验任务状态成功: taskId={}, status={}", asphaltExperimentId, status);
                return ResponseEntity.ok(new ApiResponse<>(true, "获取实验任务状态成功", status));
            } else {
                logger.warn("获取实验任务状态失败，未找到匹配的任务: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务", null));
            }
        } catch (Exception e) {
            logger.error("获取实验任务状态失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取实验任务状态失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取实验类型状态
     *
     * @param asphaltExperimentId 任务ID
     * @param currentUser 当前用户
     * @return 响应，包含实验类型到状态的映射
     */
    @GetMapping("/experiment-type-status/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Map<String, String>>> getExperimentTypeStatus(
            @PathVariable String asphaltExperimentId,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 获取实验类型状态, 任务ID: {}", currentUser.getUsername(), asphaltExperimentId);
        
        try {
            // 使用支持字符串ID的方法获取各个实验类型的状态
            Map<String, String> statusMap = asphaltTaskService.getExperimentTypeStatusByStringId(asphaltExperimentId);
            
            if (statusMap != null && !statusMap.isEmpty()) {
                logger.info("获取实验类型状态成功: taskId={}, 状态数量={}", asphaltExperimentId, statusMap.size());
                for (Map.Entry<String, String> entry : statusMap.entrySet()) {
                    logger.debug("实验类型: {}, 状态: {}", entry.getKey(), entry.getValue());
                }
                return ResponseEntity.ok(new ApiResponse<>(true, "获取实验类型状态成功", statusMap));
            } else {
                logger.warn("获取实验类型状态失败，未找到匹配的任务或无实验类型: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务或无实验类型", new HashMap<>()));
            }
        } catch (Exception e) {
            logger.error("获取实验类型状态失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取实验类型状态失败: " + e.getMessage(), new HashMap<>()));
        }
    }

    /**
     * 更新特定实验类型的状态为已完成（新接口，使用查询参数）
     *
     * @param asphaltExperimentId 任务ID（查询参数）
     * @param experimentType 实验类型（查询参数）
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/update-experiment-type-status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> updateExperimentTypeStatusToFinished(
            @RequestParam String asphaltExperimentId,
            @RequestParam String experimentType,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 通过新接口更新实验任务状态为已完成, 任务ID: {}, 实验类型: {}", 
            currentUser.getUsername(), asphaltExperimentId, experimentType);
        
        try {
            // 使用支持字符串ID的方法，避免Long类型转换错误
            AsphaltTask updatedTask = asphaltTaskService.updateExperimentStatusToFinishedByStringId(asphaltExperimentId, experimentType);
            
            if (updatedTask != null) {
                logger.info("成功更新实验任务状态为已完成: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(true, "实验任务状态更新成功", true));
            } else {
                logger.warn("更新实验任务状态失败，未找到匹配的任务: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务", false));
            }
        } catch (Exception e) {
            logger.error("更新实验任务状态失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "更新实验任务状态失败: " + e.getMessage(), false));
        }
    }

    /**
     * 更新沥青实验设备信息
     * @param asphaltExperimentId 任务ID（查询参数）
     * @param equipment 设备型号（查询参数）
     * @param manufacturer 设备厂家（查询参数）
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping("/update-equipment-info")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> updateEquipmentInfo(
            @RequestParam String asphaltExperimentId,
            @RequestParam String equipment,
            @RequestParam String manufacturer,
            @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 更新沥青实验设备信息, 任务ID: {}, 设备型号: {}, 设备厂家: {}", 
            currentUser.getUsername(), asphaltExperimentId, equipment, manufacturer);
        
        try {
            // 使用支持字符串ID的方法，避免Long类型转换错误
            AsphaltTask updatedTask = asphaltTaskService.updateEquipmentInfo(asphaltExperimentId, equipment, manufacturer);
            
            if (updatedTask != null) {
                logger.info("成功更新沥青实验设备信息: taskId={}, equipment={}, manufacturer={}", 
                    asphaltExperimentId, equipment, manufacturer);
                return ResponseEntity.ok(new ApiResponse<>(true, "沥青实验设备信息更新成功", true));
            } else {
                logger.warn("更新设备信息失败，未找到匹配的任务: {}", asphaltExperimentId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到匹配的实验任务", false));
            }
        } catch (Exception e) {
            logger.error("更新沥青实验设备信息失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "更新沥青实验设备信息失败: " + e.getMessage(), false));
        }
    }

    /**
     * 获取沥青实验任务指派信息
     * 
     * @param asphaltExperimentId 任务ID
     * @return 包含任务指派信息和设备信息的响应
     */
    @GetMapping("/task-assignment/{asphaltExperimentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AsphaltTaskAssignmentResponse>> getTaskAssignment(@PathVariable("asphaltExperimentId") String asphaltExperimentId) {
        logger.info("获取任务ID为{}的沥青实验任务指派信息", asphaltExperimentId);
        
        try {
            Map<String, String> taskInfo = asphaltTaskService.getTaskAssignmentAndEquipment(asphaltExperimentId);
            
            if (taskInfo != null) {
                AsphaltTaskAssignmentResponse response = new AsphaltTaskAssignmentResponse();
                response.setTaskId(asphaltExperimentId);
                response.setTaskAssignment(taskInfo.get("taskAssignment"));
                response.setAssignedAsphaltEquipment(taskInfo.get("assignedAsphaltEquipment"));
                response.setAsphaltEquipmentManufacturer(taskInfo.get("asphaltEquipmentManufacturer"));
                
                return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青实验任务指派信息成功", response));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到任务指派信息", null));
            }
        } catch (Exception e) {
            logger.error("获取沥青实验任务指派信息失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取沥青实验任务指派信息失败: " + e.getMessage(), null));
        }
    }
}
