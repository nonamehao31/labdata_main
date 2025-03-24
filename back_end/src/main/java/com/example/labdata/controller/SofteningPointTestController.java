package com.example.labdata.controller;

import com.example.labdata.model.SofteningPointTest;
import com.example.labdata.payload.request.SofteningPointTestRequest;
import com.example.labdata.service.SofteningPointTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 软化点试验控制器
 */
@RestController
@RequestMapping("/api/softening-point")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SofteningPointTestController {
    
    private static final Logger logger = LoggerFactory.getLogger(SofteningPointTestController.class);
    
    @Autowired
    private SofteningPointTestService softeningPointTestService;
    
    /**
     * 提交软化点试验数据
     * 
     * @param request 软化点试验数据请求
     * @return 响应结果
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> submitSofteningPointTest(@RequestBody SofteningPointTestRequest request) {
        logger.info("用户 {} 提交软化点实验数据，任务ID: {}", request.getExperimenter(), request.getTaskId());
        
        boolean success = softeningPointTestService.submitSofteningPointTest(request);
        if (success) {
            return ResponseEntity.ok().body("软化点试验数据提交成功");
        } else {
            return ResponseEntity.badRequest().body("软化点试验数据提交失败");
        }
    }
    
    /**
     * 获取指定任务ID的软化点试验数据
     * 
     * @param taskId 任务ID
     * @return 响应结果
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSofteningPointTestByTaskId(@PathVariable String taskId) {
        logger.info("查询任务ID为{}的软化点试验数据", taskId);
        
        Optional<SofteningPointTest> softeningPointTest = softeningPointTestService.getSofteningPointTestByTaskId(taskId);
        return softeningPointTest
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取所有软化点试验数据
     * 
     * @return 响应结果
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SofteningPointTest>> getAllSofteningPointTests() {
        logger.info("查询所有软化点试验数据");
        
        List<SofteningPointTest> softeningPointTests = softeningPointTestService.getAllSofteningPointTests();
        return ResponseEntity.ok(softeningPointTests);
    }
    
    /**
     * 获取指定操作人的所有软化点试验数据
     * 
     * @param experimenter 操作人
     * @return 响应结果
     */
    @GetMapping("/experimenter/{experimenter}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SofteningPointTest>> getSofteningPointTestsByExperimenter(@PathVariable String experimenter) {
        logger.info("查询操作人为{}的软化点试验数据", experimenter);
        
        List<SofteningPointTest> softeningPointTests = softeningPointTestService.getSofteningPointTestsByExperimenter(experimenter);
        return ResponseEntity.ok(softeningPointTests);
    }
    
    /**
     * 获取指定软化温度范围的所有试验数据
     * 
     * @param minTemp 最小软化温度
     * @param maxTemp 最大软化温度
     * @return 响应结果
     */
    @GetMapping("/temperature-range")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SofteningPointTest>> getSofteningPointTestsByTemperatureRange(
            @RequestParam double minTemp,
            @RequestParam double maxTemp) {
        logger.info("查询软化温度范围在{}到{}的试验数据", minTemp, maxTemp);
        
        List<SofteningPointTest> softeningPointTests = softeningPointTestService.getSofteningPointTestsByTemperatureRange(minTemp, maxTemp);
        return ResponseEntity.ok(softeningPointTests);
    }
}
