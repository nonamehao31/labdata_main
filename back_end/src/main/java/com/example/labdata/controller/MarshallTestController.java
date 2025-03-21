package com.example.labdata.controller;

import com.example.labdata.model.MarshallTest;
import com.example.labdata.payload.request.MarshallTestRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.MarshallTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 马歇尔试验数据控制器
 */
@RestController
@RequestMapping("/api/marshall-test")
public class MarshallTestController {

    private static final Logger logger = LoggerFactory.getLogger(MarshallTestController.class);

    @Autowired
    private MarshallTestService marshallTestService;

    /**
     * 保存马歇尔试验数据
     *
     * @param request 马歇尔试验数据请求
     * @param currentUser 当前用户
     * @return 响应
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> saveMarshallTest(@RequestBody MarshallTestRequest request,
                                             @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 请求保存马歇尔试验数据, 任务ID: {}", currentUser.getId(), request.getTaskId());
        
        try {
            MarshallTest savedTest = marshallTestService.saveMarshallTest(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedTest.getId());
            response.put("message", "马歇尔试验数据保存成功");
            
            return ResponseEntity.ok(new ApiResponse(true, "马歇尔试验数据保存成功", response));
        } catch (Exception e) {
            logger.error("保存马歇尔试验数据失败", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "保存马歇尔试验数据失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定任务的马歇尔试验数据
     *
     * @param taskId 任务ID
     * @param currentUser 当前用户
     * @return 响应
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMarshallTestsByTaskId(@PathVariable String taskId,
                                                    @CurrentUser UserPrincipal currentUser) {
        logger.info("用户 {} 请求获取任务 {} 的马歇尔试验数据", currentUser.getId(), taskId);
        
        try {
            List<MarshallTest> tests = marshallTestService.getMarshallTestsByTaskId(taskId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("tests", tests);
            
            return ResponseEntity.ok(new ApiResponse(true, "获取马歇尔试验数据成功", response));
        } catch (Exception e) {
            logger.error("获取马歇尔试验数据失败", e);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "获取马歇尔试验数据失败: " + e.getMessage()));
        }
    }
}
