package com.example.labdata.controller;

import com.example.labdata.entity.DynamicShearRheometerTest;
import com.example.labdata.payload.dto.DsrTestResult;
import com.example.labdata.payload.request.DynamicShearRheometerTestRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.DynamicShearRheometerTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态剪切流变仪实验控制器
 */
@RestController
@RequestMapping("/api/dsr")
public class DynamicShearRheometerTestController {

    @Autowired
    private DynamicShearRheometerTestService dynamicShearRheometerTestService;

    /**
     * 提交动态剪切流变仪实验数据
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> submitTestData(@RequestBody DynamicShearRheometerTestRequest request) {
        try {
            DynamicShearRheometerTest savedTest = dynamicShearRheometerTestService.saveTestData(request);
            // 注意：返回Boolean.TRUE而不是ID，避免前后端类型不匹配问题
            return ResponseEntity.ok(new ApiResponse(true, "动态剪切流变仪实验数据提交成功", Boolean.TRUE));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "动态剪切流变仪实验数据提交失败: " + e.getMessage()));
        }
    }

    /**
     * 获取动态剪切流变仪实验数据
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getTestDataByTaskId(@PathVariable String taskId) {
        try {
            List<DsrTestResult> testResults = dynamicShearRheometerTestService.getTestDataByTaskId(taskId);
            return ResponseEntity.ok(new ApiResponse(true, "成功获取动态剪切流变仪实验数据", testResults));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取动态剪切流变仪实验数据失败: " + e.getMessage()));
        }
    }
}