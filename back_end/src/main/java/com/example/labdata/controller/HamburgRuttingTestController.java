package com.example.labdata.controller;

import com.example.labdata.model.HamburgRuttingTest;
import com.example.labdata.service.HamburgRuttingTestService;
import com.example.labdata.payload.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汉堡车辙实验数据控制器
 */
@RestController
@RequestMapping("/api")
public class HamburgRuttingTestController {

    private static final Logger logger = LoggerFactory.getLogger(HamburgRuttingTestController.class);
    private final HamburgRuttingTestService hamburgRuttingTestService;

    @Autowired
    public HamburgRuttingTestController(HamburgRuttingTestService hamburgRuttingTestService) {
        this.hamburgRuttingTestService = hamburgRuttingTestService;
    }

    /**
     * 保存汉堡车辙实验数据
     *
     * @param requestData 包含实验数据的请求体
     * @return 保存结果
     */
    @PostMapping("/hamburg-rutting-test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveHamburgRuttingTest(@RequestBody Map<String, Object> requestData) {
        logger.info("接收到保存汉堡车辙实验数据请求: {}", requestData);
        
        try {
            // 从请求数据中提取必要信息
            String taskId = (String) requestData.get("taskId");
            Object mixRatioIdObj = requestData.get("mixRatioId");
            Long mixRatioId = null;
            
            if (mixRatioIdObj instanceof Number) {
                mixRatioId = ((Number) mixRatioIdObj).longValue();
            } else if (mixRatioIdObj instanceof String) {
                mixRatioId = Long.parseLong((String) mixRatioIdObj);
            }
            
            // 从请求体中获取实验数据
            Float steadySlope1 = parseFloat(requestData.get("steadySlope1"));
            Float steadyCurvilinear1 = parseFloat(requestData.get("steadyCurvilinear1"));
            Float steadySlope2 = parseFloat(requestData.get("steadySlope2"));
            Float steadyCurvilinear2 = parseFloat(requestData.get("steadyCurvilinear2"));
            
            // 创建数据模型并保存
            HamburgRuttingTest hamburgRuttingTest = new HamburgRuttingTest();
            hamburgRuttingTest.setTaskId(taskId);
            hamburgRuttingTest.setMixRatioId(mixRatioId);
            hamburgRuttingTest.setSteadySlope1(steadySlope1);
            hamburgRuttingTest.setSteadyCurvilinear1(steadyCurvilinear1);
            hamburgRuttingTest.setSteadySlope2(steadySlope2);
            hamburgRuttingTest.setSteadyCurvilinear2(steadyCurvilinear2);
            
            // 保存数据
            HamburgRuttingTest savedData = hamburgRuttingTestService.saveHamburgRuttingTest(hamburgRuttingTest);
            
            // 构建响应数据
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("id", savedData.getId());
            responseData.put("taskId", savedData.getTaskId());
            responseData.put("mixRatioId", savedData.getMixRatioId());
            responseData.put("createdAt", savedData.getCreatedAt());
            
            logger.info("成功保存汉堡车辙实验数据，ID: {}", savedData.getId());
            
            return ResponseEntity.ok(new ApiResponse<>(true, "汉堡车辙实验数据保存成功", responseData));
        } catch (Exception e) {
            logger.error("保存汉堡车辙实验数据时出错", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "保存数据时出错: " + e.getMessage(), null));
        }
    }

    /**
     * 获取指定任务的汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据列表
     */
    @GetMapping("/tasks/{taskId}/hamburg-rutting-tests")
    public ResponseEntity<ApiResponse<List<HamburgRuttingTest>>> getHamburgRuttingTestsByTaskId(@PathVariable String taskId) {
        try {
            List<HamburgRuttingTest> tests = hamburgRuttingTestService.getHamburgRuttingTestsByTaskId(taskId);
            return ResponseEntity.ok(new ApiResponse<>(true, "获取数据成功", tests));
        } catch (Exception e) {
            logger.error("获取汉堡车辙实验数据时出错", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取数据时出错: " + e.getMessage(), null));
        }
    }

    /**
     * 获取指定任务和配比的汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 汉堡车辙实验数据
     */
    @GetMapping("/tasks/{taskId}/mix-ratios/{mixRatioId}/hamburg-rutting-test")
    public ResponseEntity<ApiResponse<HamburgRuttingTest>> getHamburgRuttingTestByTaskIdAndMixRatioId(
            @PathVariable String taskId, @PathVariable Long mixRatioId) {
        try {
            HamburgRuttingTest test = hamburgRuttingTestService.getHamburgRuttingTestByTaskIdAndMixRatioId(taskId, mixRatioId);
            if (test != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "获取数据成功", test));
            } else {
                return ResponseEntity.ok(new ApiResponse<>(false, "未找到汉堡车辙实验数据", null));
            }
        } catch (Exception e) {
            logger.error("获取汉堡车辙实验数据时出错", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取数据时出错: " + e.getMessage(), null));
        }
    }
    
    /**
     * 安全地解析Float值
     *
     * @param value 要解析的值
     * @return 解析后的Float值，如果为null或无法解析则返回null
     */
    private Float parseFloat(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                logger.warn("无法解析Float值: {}", value);
                return null;
            }
        }
        
        return null;
    }
}
