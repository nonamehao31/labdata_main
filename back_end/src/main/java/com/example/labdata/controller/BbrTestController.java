package com.example.labdata.controller;

import com.example.labdata.entity.BbrTest;
import com.example.labdata.payload.request.BbrTestRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.BbrTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 沥青弯曲蠕变劲度试验（弯曲梁流变仪法）控制器
 */
@RestController
@RequestMapping("/api/bbr")
public class BbrTestController {
    
    private final BbrTestService bbrTestService;
    
    @Autowired
    public BbrTestController(BbrTestService bbrTestService) {
        this.bbrTestService = bbrTestService;
    }
    
    /**
     * 提交试验数据
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> submitTestData(@RequestBody BbrTestRequest request) {
        try {
            BbrTest savedTest = bbrTestService.saveTestData(request);
            return ResponseEntity.ok(new ApiResponse(true, "提交成功", Boolean.TRUE));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "提交失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取特定任务的所有试验数据
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getTestsByTaskId(@PathVariable String taskId) {
        try {
            List<BbrTest> tests = bbrTestService.getTestsByTaskId(taskId);
            return ResponseEntity.ok(new ApiResponse(true, "获取成功", tests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }
}
