package com.example.labdata.controller;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.model.SupportMixtureTask;
import com.example.labdata.model.MixtureBendingTest;
import com.example.labdata.model.MarshallTest;
import com.example.labdata.model.HamburgRuttingTest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.MixratioSpecimenPairResponse;
import com.example.labdata.payload.response.MixRatioDetailResponse;
import com.example.labdata.payload.response.MixratioAndCompactionResponse;
import com.example.labdata.payload.response.ProjectNameResponse;
import com.example.labdata.payload.response.TaskAssignmentResponse;
import com.example.labdata.service.MixtureTaskService;
import com.example.labdata.service.MarshallTestService;
import com.example.labdata.service.HamburgRuttingTestService;
import com.example.labdata.service.MixtureBendingTestService;
import com.example.labdata.payload.response.MarshallTestDTO;
import com.example.labdata.payload.response.HamburgRuttingTestDTO;
import com.example.labdata.payload.response.MixtureBendingTestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class MixtureTaskController {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskController.class);
    private static final Logger log = LoggerFactory.getLogger(MixtureTaskController.class);
    

    @Autowired
    private MixtureTaskService mixtureTaskService;

    @Autowired
    private MarshallTestService marshallTestService;

    @Autowired
    private HamburgRuttingTestService hamburgRuttingTestService;

    @Autowired
    private MixtureBendingTestService mixtureBendingTestService;

    /**
     * 获取混合料任务的测试状态
     *
     * @param taskId 任务ID
     * @return 测试状态
     */
    @GetMapping("/mixture-tasks/{taskId}/testing-status")
    public ApiResponse<String> getTestingStatus(@PathVariable String taskId) {
        try {
            String status = mixtureTaskService.getTestingStatus(taskId);
            return new ApiResponse<>(true, "成功获取测试状态", status);
        } catch (Exception e) {
            logger.error("获取测试状态时发生错误: ", e);
            return new ApiResponse<>(false, "获取测试状态时发生错误: " + e.getMessage(), "error");
        }
    }

    /**
     * 获取混合料任务的配比名称和压实方法
     *
     * @param taskId 任务ID
     * @return 配比名称和压实方法信息
     */
    @GetMapping("/mixtureTask/getMixratioAndCompaction/{taskId}")
    public ResponseEntity<MixratioAndCompactionResponse> getMixratioAndCompaction(@PathVariable String taskId) {
        try {
            MixratioAndCompactionResponse result = mixtureTaskService.getMixratioAndCompaction(taskId);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.ok(new MixratioAndCompactionResponse(404, "未找到配比和压实方法信息"));
            }
        } catch (Exception e) {
            logger.error("获取配比和压实方法信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MixratioAndCompactionResponse(500, "获取配比和压实方法信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取混合料任务中各实验类型的状态
     *
     * @param taskId 任务ID
     * @return 各实验类型的状态映射（实验类型 -> 状态）
     */
    @GetMapping("/mixture-tasks/{taskId}/experiment-type-status")
    public ApiResponse<List<Map<String, String>>> getExperimentTypeStatus(@PathVariable String taskId) {
        try {
            logger.info("正在获取任务ID为 {} 的实验类型状态", taskId);
            List<Map<String, String>> statusList = mixtureTaskService.getExperimentTypeStatusByTaskId(taskId);
            logger.info("成功获取任务ID为 {} 的实验类型状态: {}", taskId, statusList);
            return new ApiResponse<>(true, "成功获取任务状态信息", statusList);
        } catch (Exception e) {
            logger.error("获取实验类型状态时发生错误: ", e);
            return new ApiResponse<>(false, "获取实验类型状态时发生错误: " + e.getMessage(), new ArrayList<>());
        }
    }

    @GetMapping("/mixtureTask/list")
    public ResponseEntity<List<MixtureTask>> getAllMixtureTasks() {
        return ResponseEntity.ok(mixtureTaskService.getAllMixtureTasks());
    }
    
    @GetMapping("/mixtureTask/listByType")
    public ResponseEntity<List<MixtureTask>> getMixtureTasksByType(@RequestParam String taskType) {
        return ResponseEntity.ok(mixtureTaskService.getMixtureTasksByType(taskType));
    }
    


    
    /**
     * 获取支持的混合料任务类型列表
     * @param taskType 任务类型
     * @return 支持的任务类型列表
     */
    @GetMapping("/mixtureTask/supportedTasks")
    public ResponseEntity<List<SupportMixtureTask>> getSupportedMixtureTasks(@RequestParam String taskType) {
        return ResponseEntity.ok(mixtureTaskService.getSupportedMixtureTasks(taskType));
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
     * 根据任务ID获取任务详细信息
     * 
     * @param taskId 任务ID
     * @return 任务详细信息
     */
    @GetMapping("/mixtureTask/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMixtureTaskByTaskId(@PathVariable String taskId) {
        logger.info("接收到获取任务详情请求，任务ID: {}", taskId);
        try {
            Map<String, Object> taskData = mixtureTaskService.getMixtureTaskDataByTaskId(taskId);
            if (taskData == null || taskData.isEmpty()) {
                logger.warn("未找到任务ID={}的详细信息", taskId);
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到任务详情", null));
            }
            logger.info("成功获取任务ID={}的详细信息", taskId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取任务详情成功", taskData));
        } catch (Exception e) {
            logger.error("获取任务详情时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取任务详情失败: " + e.getMessage(), null));
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


    /**
     * 保存动态模量试验数据
     * 
     * @param requestData 请求数据
     * @return 保存结果
     */
    @PostMapping("/mixtureTask/saveDynamicModulusTest")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveDynamicModulusTest(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("接收到保存动态模量试验数据请求: {}", requestData);
            
            String taskId = (String) requestData.get("taskId");
            String mixRatioId = (String) requestData.get("mixRatioId");
            
            if (taskId == null || mixRatioId == null) {
                logger.error("缺少必要参数: taskId={}, mixRatioId={}", taskId, mixRatioId);
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "缺少必要参数"));
            }
            
            // 调用服务层方法保存数据
            Map<String, Object> result = mixtureTaskService.saveDynamicModulusTest(requestData);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "保存成功", result));
        } catch (Exception e) {
            logger.error("保存动态模量试验数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "保存动态模量试验数据失败: " + e.getMessage()));
        }
    }

    /**
     * 保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据
     * 
     * @param requestData 请求数据
     * @return 保存结果
     */
    @PostMapping("/mixtureTask/saveDirectStretchingFatigueTestData")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveDirectStretchingFatigueTestData(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("接收到保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据请求");
            
            String taskId = (String) requestData.get("taskId");
            String mixRatioId = (String) requestData.get("mixRatioId");
            
            if (taskId == null || mixRatioId == null) {
                logger.error("缺少必要参数: taskId={}, mixRatioId={}", taskId, mixRatioId);
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "缺少必要参数"));
            }
            
            // 检查specimens字段是否存在
            if (!requestData.containsKey("specimens") || requestData.get("specimens") == null) {
                logger.error("请求中缺少specimens字段或其值为null");
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "试件数据为空，请提供有效的试件信息"));
            }
            
            // 检查testInfo字段是否存在
            if (!requestData.containsKey("testInfo") || requestData.get("testInfo") == null) {
                logger.error("请求中缺少testInfo字段或其值为null");
                return ResponseEntity.badRequest().body(new ApiResponse<>(false, "测试信息为空，请提供有效的测试信息"));
            }
            
            // 记录完整请求数据以便调试
            logger.debug("完整请求数据: {}", requestData);
            
            // 调用服务层方法保存数据
            Map<String, String> result = mixtureTaskService.saveDirectStretchingFatigueTestData(requestData);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "保存成功", result));
        } catch (Exception e) {
            logger.error("保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 保存沥青混合料四点弯曲疲劳寿命试验数据
     * 
     * @param requestData 包含试验数据的请求Map
     * @return 保存结果
     */
    @PostMapping("/mixtureTask/saveFourPointFatigueTestData")
    public ResponseEntity<?> saveFourPointFatigueTestData(@RequestBody Map<String, Object> requestData) {
        try {
            Map<String, String> result = mixtureTaskService.saveFourPointFatigueTestData(requestData);
            return ResponseEntity.ok(new ApiResponse<>(true, "四点弯曲疲劳寿命试验数据保存成功", result));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "四点弯曲疲劳寿命试验数据保存失败: " + e.getMessage(), null));
        }
    }

    /**
     * 保存单轴压缩试验数据
     * 
     * @param requestData 包含测试数据的Map
     * @return 保存结果
     */
    @PostMapping("/mixtureTask/saveUniaxialCompressionTestData")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveUniaxialCompressionTestData(
            @RequestBody Map<String, Object> requestData) {
        logger.info("接收到单轴压缩试验数据保存请求: {}", requestData);
        try {
            Map<String, String> result = mixtureTaskService.saveUniaxialCompressionTestData(requestData);
            return ResponseEntity.ok(new ApiResponse<>(true, "单轴压缩试验数据保存成功", result));
        } catch (Exception e) {
            logger.error("保存单轴压缩试验数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "保存单轴压缩试验数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 保存劈裂试验数据
     */
    @PostMapping("/mixtureTask/saveSplittingTestData")
    public ResponseEntity<ApiResponse<Map<String, String>>> saveSplittingTestData(
            @RequestBody Map<String, Object> requestData) {
        try {
            Map<String, String> result = mixtureTaskService.saveSplittingTestData(requestData);
            return ResponseEntity.ok(new ApiResponse<>(true, "劈裂试验数据保存成功", result));
        } catch (Exception e) {
            log.error("保存劈裂试验数据时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "保存劈裂试验数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取混合料任务的任务指派信息
     *
     * @param taskId 任务ID
     * @return 任务指派信息
     */
    @GetMapping("/mixtureTask/getTaskAssignment/{taskId}")
    public ResponseEntity<TaskAssignmentResponse> getTaskAssignment(@PathVariable String taskId) {
        try {
            String taskAssignment = mixtureTaskService.getTaskAssignment(taskId);
            if (taskAssignment != null) {
                return ResponseEntity.ok(new TaskAssignmentResponse(taskAssignment));
            } else {
                return ResponseEntity.ok(new TaskAssignmentResponse(404, "未找到任务指派信息"));
            }
        } catch (Exception e) {
            logger.error("获取任务指派信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TaskAssignmentResponse(500, "获取任务指派信息失败: " + e.getMessage()));
        }
    }

    /**
     * 获取马歇尔稳定度试验数据
     *
     * @param taskId 任务ID
     * @return 马歇尔稳定度试验数据列表
     */
    @GetMapping("/mixtureTask/getMarshallTest/{taskId}")
    public ResponseEntity<List<MarshallTestDTO>> getMarshallTestByTaskId(@PathVariable String taskId) {
        logger.info("接收到获取马歇尔试验数据请求，任务ID: {}", taskId);
        try {
            List<MarshallTest> tests = marshallTestService.getMarshallTestsByTaskId(taskId);
            
            // 将实体对象转换为DTO，确保字段名符合前端期望的格式
            List<MarshallTestDTO> dtoList = tests.stream()
                .map(MarshallTestDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
                
            logger.info("成功获取任务ID={}的马歇尔试验数据，共{}条", taskId, dtoList.size());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("获取马歇尔试验数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据列表
     */
    @GetMapping("/mixtureTask/getHamburgRuttingTest/{taskId}")
    public ResponseEntity<List<HamburgRuttingTestDTO>> getHamburgRuttingTestByTaskId(@PathVariable String taskId) {
        logger.info("接收到获取汉堡车辙实验数据请求，任务ID: {}", taskId);
        try {
            List<HamburgRuttingTest> tests = hamburgRuttingTestService.getHamburgRuttingTestsByTaskId(taskId);
            
            // 将实体对象转换为DTO，确保字段名符合前端期望的格式
            List<HamburgRuttingTestDTO> dtoList = tests.stream()
                .map(HamburgRuttingTestDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
                
            logger.info("成功获取任务ID={}的汉堡车辙实验数据，共{}条", taskId, dtoList.size());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("获取汉堡车辙实验数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取沥青混合料弯曲试验数据
     *
     * @param taskId 任务ID
     * @return 沥青混合料弯曲试验数据列表
     */
    @GetMapping("/mixtureTask/getMixtureBendingTest/{taskId}")
    public ResponseEntity<List<MixtureBendingTestDTO>> getMixtureBendingTestByTaskId(@PathVariable String taskId) {
        logger.info("接收到获取沥青混合料弯曲试验数据请求，任务ID: {}", taskId);
        try {
            List<MixtureBendingTest> tests = mixtureBendingTestService.getMixtureBendingTestsByTaskId(taskId);
            
            // 将实体对象转换为DTO，确保字段名符合前端期望的格式
            List<MixtureBendingTestDTO> dtoList = tests.stream()
                .map(MixtureBendingTestDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
                
            logger.info("成功获取任务ID={}的沥青混合料弯曲试验数据，共{}条", taskId, dtoList.size());
            return ResponseEntity.ok(dtoList);
        } catch (Exception e) {
            logger.error("获取沥青混合料弯曲试验数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 获取动态模量试验数据
     *
     * @param taskId 任务ID
     * @return 动态模量试验数据
     */
    @GetMapping("/mixtureTask/getDynamicModulusTest/{taskId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDynamicModulusTest(@PathVariable String taskId) {
        try {
            logger.info("接收到获取动态模量试验数据请求: taskId={}", taskId);

            List<Map<String, Object>> result = mixtureTaskService.getDynamicModulusTestByTaskId(taskId);

            if (result.isEmpty()) {
                logger.warn("未找到任务ID: {} 的动态模量试验数据", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "未找到动态模量试验数据", new ArrayList<>()));
            }

            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", result));
        } catch (Exception e) {
            logger.error("获取动态模量试验数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取动态模量试验数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
     * 
     * @param taskId 任务ID
     * @return 沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据列表
     */
    @GetMapping("/mixtureTask/getDirectStretchingFatigueTest/{taskId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getDirectStretchingFatigueTest(@PathVariable String taskId) {
        logger.info("接收到获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据请求，任务ID: {}", taskId);
        try {
            List<Map<String, Object>> testResults = mixtureTaskService.getDirectStretchingFatigueTestByTaskId(taskId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据成功", testResults));
        } catch (Exception e) {
            logger.error("获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取沥青混合料四点弯曲疲劳寿命实验数据
     *
     * @param taskId 任务ID
     * @return 实验数据响应
     */
    @GetMapping("/mixtureTask/getFourPointBendingTest/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFourPointBendingTestByTaskId(@PathVariable String taskId) {
        try {
            logger.info("接收到获取沥青混合料四点弯曲疲劳寿命实验数据请求，任务ID: {}", taskId);
            Map<String, Object> testData = mixtureTaskService.getFourPointBendingTestData(taskId);
            if (testData != null && !testData.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青混合料四点弯曲疲劳寿命实验数据成功", testData));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到沥青混合料四点弯曲疲劳寿命实验数据", null));
            }
        } catch (Exception e) {
            logger.error("获取沥青混合料四点弯曲疲劳寿命实验数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取沥青混合料四点弯曲疲劳寿命实验数据失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取沥青混合料单轴压缩试验（圆柱体法）数据
     *
     * @param taskId 任务ID
     * @return 实验数据响应
     */
    @GetMapping("/mixtureTask/getUniaxialCompressionTest/{taskId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUniaxialCompressionTestByTaskId(@PathVariable String taskId) {
        try {
            logger.info("接收到获取沥青混合料单轴压缩试验（圆柱体法）数据请求，任务ID: {}", taskId);
            Map<String, Object> testData = mixtureTaskService.getUniaxialCompressionTestData(taskId);
            if (testData != null && !testData.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "获取沥青混合料单轴压缩试验（圆柱体法）数据成功", testData));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到沥青混合料单轴压缩试验（圆柱体法）数据", null));
            }
        } catch (Exception e) {
            logger.error("获取沥青混合料单轴压缩试验（圆柱体法）数据时出错: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取沥青混合料单轴压缩试验（圆柱体法）数据失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取沥青混合料劈裂试验数据
     *
     * @param taskId 任务ID
     * @return 沥青混合料劈裂试验数据列表
     */
    @GetMapping("/mixtureTask/getSplittingTest/{taskId}")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getSplittingTestByTaskId(@PathVariable String taskId) {
        logger.info("接收到获取沥青混合料劈裂试验数据请求，任务ID: {}", taskId);
        try {
            List<Map<String, Object>> result = mixtureTaskService.getSplittingTestByTaskId(taskId);
            
            if (result.isEmpty()) {
                logger.warn("未找到任务ID: {} 的沥青混合料劈裂试验数据", taskId);
                return ResponseEntity.ok(new ApiResponse<>(true, "未找到沥青混合料劈裂试验数据", new ArrayList<>()));
            }
            
            logger.info("成功获取任务ID={}的沥青混合料劈裂试验数据，共{}条", taskId, result.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取成功", result));
        } catch (Exception e) {
            logger.error("获取沥青混合料劈裂试验数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "获取沥青混合料劈裂试验数据失败: " + e.getMessage(), null));
        }
    }
}
