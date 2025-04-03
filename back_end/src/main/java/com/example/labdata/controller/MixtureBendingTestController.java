package com.example.labdata.controller;

import com.example.labdata.model.MixtureBendingTest;
import com.example.labdata.payload.response.MixtureBendingTestDTO;
import com.example.labdata.service.MixtureBendingTestService;
import com.example.labdata.service.MixtureTaskStatusService;
import com.example.labdata.payload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 沥青混合料弯曲试验控制器
 */
@RestController
@RequestMapping("/api")
public class MixtureBendingTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(MixtureBendingTestController.class);
    private final MixtureBendingTestService mixtureBendingTestService;
    private final ObjectMapper objectMapper;
    private final MixtureTaskStatusService mixtureTaskStatusService;
    
    public MixtureBendingTestController(MixtureBendingTestService mixtureBendingTestService, 
                                     ObjectMapper objectMapper,
                                     MixtureTaskStatusService mixtureTaskStatusService) {
        this.mixtureBendingTestService = mixtureBendingTestService;
        this.objectMapper = objectMapper;
        this.mixtureTaskStatusService = mixtureTaskStatusService;
    }
    
    /**
     * 保存沥青混合料弯曲试验数据
     * 
     * @param requestData 包含任务ID、配比ID和弯曲试验数据的请求体
     * @return 保存结果
     */
    @PostMapping("/mixture-bending-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveMixtureBendingTest(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("接收到保存沥青混合料弯曲试验数据请求: {}", requestData);
            
            // 提取基本参数
            String taskId = (String) requestData.get("taskId");
            Long mixRatioId = Long.valueOf(requestData.get("mixRatioId").toString());
            
            if (taskId == null || mixRatioId == null) {
                logger.error("缺少必要参数: taskId={}, mixRatioId={}", taskId, mixRatioId);
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "缺少必要参数: taskId 和 mixRatioId", null));
            }
            
            // 提取其他参数
            Float spanLength = requestData.get("spanLength") != null ? 
                Float.valueOf(requestData.get("spanLength").toString()) : null;
            Integer specimenCount = requestData.get("specimenCount") != null ? 
                Integer.valueOf(requestData.get("specimenCount").toString()) : 0;
            Float averageFlexuralStrength = requestData.get("averageFlexuralStrength") != null ? 
                Float.valueOf(requestData.get("averageFlexuralStrength").toString()) : null;
            Float averageMaxStrain = requestData.get("averageMaxStrain") != null ? 
                Float.valueOf(requestData.get("averageMaxStrain").toString()) : null;
            Float averageStiffnessModulus = requestData.get("averageStiffnessModulus") != null ? 
                Float.valueOf(requestData.get("averageStiffnessModulus").toString()) : null;
            
            // 提取试件数据，并转换为JSON字符串
            Object specimens = requestData.get("specimens");
            String specimensJson = specimens != null ? objectMapper.writeValueAsString(specimens) : null;
            
            // 创建数据模型对象
            MixtureBendingTest mixtureBendingTest = new MixtureBendingTest();
            mixtureBendingTest.setTaskId(taskId);
            mixtureBendingTest.setMixRatioId(mixRatioId);
            mixtureBendingTest.setSpanLength(spanLength);
            mixtureBendingTest.setSpecimenCount(specimenCount);
            mixtureBendingTest.setAverageFlexuralStrength(averageFlexuralStrength);
            mixtureBendingTest.setAverageMaxStrain(averageMaxStrain);
            mixtureBendingTest.setAverageStiffnessModulus(averageStiffnessModulus);
            mixtureBendingTest.setSpecimens(specimensJson);
            
            // 保存数据
            MixtureBendingTest savedData = mixtureBendingTestService.saveMixtureBendingTest(mixtureBendingTest);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", savedData.getId());
            responseData.put("taskId", savedData.getTaskId());
            responseData.put("mixRatioId", savedData.getMixRatioId());
            responseData.put("success", true);
            
            logger.info("沥青混合料弯曲试验数据保存成功, id={}", savedData.getId());
            
            // 更新任务状态为完成
            try {
                mixtureTaskStatusService.updateExperimentCompleteStatus(taskId, "沥青混合料弯曲试验");
                logger.info("成功更新任务状态: taskId={}, type={}", taskId, "沥青混合料弯曲试验");
            } catch (Exception e) {
                logger.warn("更新任务状态失败，但数据已保存成功: {}", e.getMessage());
                // 不影响主流程，仍返回成功响应
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "数据保存成功", responseData));
            
        } catch (Exception e) {
            logger.error("保存沥青混合料弯曲试验数据失败", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "保存数据失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据任务ID获取所有沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @return 指定任务的所有弯曲试验数据列表
     */
    @GetMapping("/mixture-bending-test/task/{taskId}")
    public ResponseEntity<ApiResponse<List<MixtureBendingTestDTO>>> getMixtureBendingTestsByTaskId(@PathVariable String taskId) {
        try {
            List<MixtureBendingTest> tests = mixtureBendingTestService.getMixtureBendingTestsByTaskId(taskId);
            
            // 将实体对象转换为DTO对象
            List<MixtureBendingTestDTO> dtoList = tests.stream()
                .map(MixtureBendingTestDTO::fromEntity)
                .collect(Collectors.toList());
                
            logger.info("查询到沥青混合料弯曲试验数据 {} 条，taskId: {}", dtoList.size(), taskId);
            // 输出第一条数据的关键字段用于调试
            if (!dtoList.isEmpty()) {
                MixtureBendingTestDTO firstDto = dtoList.get(0);
                logger.info("第一条数据示例: id={}, taskId={}, mixRatioId={}, averageFlexuralStrength={}, " +
                            "averageMaxStrain={}, averageStiffnessModulus={}",
                            firstDto.getId(), firstDto.getTaskId(), firstDto.getMixRatioId(), 
                            firstDto.getAverageFlexuralStrength(), firstDto.getAverageMaxStrain(),
                            firstDto.getAverageStiffnessModulus());
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "获取数据成功", dtoList));
        } catch (Exception e) {
            logger.error("获取沥青混合料弯曲试验数据失败", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "获取数据失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 根据任务ID和配比ID获取特定的沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 指定任务和配比的弯曲试验数据
     */
    @GetMapping("/mixture-bending-test/task/{taskId}/mix-ratio/{mixRatioId}")
    public ResponseEntity<ApiResponse<MixtureBendingTest>> getMixtureBendingTestByTaskIdAndMixRatioId(
            @PathVariable String taskId, @PathVariable Long mixRatioId) {
        try {
            MixtureBendingTest test = mixtureBendingTestService.getMixtureBendingTestByTaskIdAndMixRatioId(taskId, mixRatioId);
            if (test == null) {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到数据", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "获取数据成功", test));
        } catch (Exception e) {
            logger.error("获取沥青混合料弯曲试验数据失败", e);
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, "获取数据失败: " + e.getMessage(), null));
        }
    }
}
