package com.example.labdata.controller;

import com.example.labdata.entity.BrookfieldViscosityTest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.request.BrookfieldViscosityTestRequest;
import com.example.labdata.service.BrookfieldViscosityTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验控制器
 */
@RestController
@RequestMapping("/api/brookfield-viscosity")
public class BrookfieldViscosityTestController {

    private final BrookfieldViscosityTestService testService;

    @Autowired
    public BrookfieldViscosityTestController(BrookfieldViscosityTestService testService) {
        this.testService = testService;
    }

    /**
     * 提交布鲁克菲尔德旋转黏度实验数据
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> submitTestData(@RequestBody BrookfieldViscosityTestRequest request) {
        try {
            BrookfieldViscosityTest savedTest = testService.saveTestData(request);
            return ResponseEntity.ok(new ApiResponse(true, "布鲁克菲尔德旋转黏度实验数据提交成功", Boolean.TRUE));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "提交失败: " + e.getMessage()));
        }
    }

    /**
     * 获取特定任务的所有布鲁克菲尔德旋转黏度实验数据
     */
    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getTestsByTaskId(@PathVariable String taskId) {
        try {
            List<BrookfieldViscosityTest> tests = testService.getTestsByTaskId(taskId);
            return ResponseEntity.ok(tests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取数据失败: " + e.getMessage()));
        }
    }
}