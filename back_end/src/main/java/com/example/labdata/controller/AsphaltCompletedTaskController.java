package com.example.labdata.controller;

import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.service.AsphaltCompletedTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 沥青已完成任务控制器
 */
@RestController
@RequestMapping("/api")
public class AsphaltCompletedTaskController {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltCompletedTaskController.class);

    @Autowired
    private AsphaltCompletedTaskService asphaltCompletedTaskService;

    /**
     * 获取已完成的沥青任务
     * 
     * @param companyId 公司ID
     * @return 已完成的沥青任务列表
     */
    @GetMapping("/asphalt-tasks/completed")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCompletedAsphaltTasks(
            @RequestParam String companyId) {
        logger.info("接收到获取公司ID为 {} 的已完成沥青任务请求", companyId);
        
        try {
            List<Map<String, Object>> tasks = asphaltCompletedTaskService.getCompletedAsphaltTasks(companyId);
            logger.info("成功获取公司ID为 {} 的已完成沥青任务 {} 条", companyId, tasks.size());
            
            // 确保所有返回数据中的空值都被处理为空字符串，以避免前端解析问题
            for (Map<String, Object> task : tasks) {
                for (String key : task.keySet()) {
                    if (task.get(key) == null) {
                        task.put(key, "");
                    }
                }
            }
            
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                true, 
                "成功获取已完成的沥青任务",
                tasks
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取已完成沥青任务时发生错误", e);
            
            ApiResponse<List<Map<String, Object>>> response = new ApiResponse<>(
                false, 
                "获取已完成的沥青任务失败: " + e.getMessage(),
                null
            );
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
