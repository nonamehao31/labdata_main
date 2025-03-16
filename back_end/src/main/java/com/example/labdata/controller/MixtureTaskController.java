package com.example.labdata.controller;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.payload.response.ProjectNameResponse;
import com.example.labdata.service.MixtureTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mixtureTask")
public class MixtureTaskController {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskController.class);

    @Autowired
    private MixtureTaskService mixtureTaskService;

    @GetMapping("/list")
    public ResponseEntity<List<MixtureTask>> getAllMixtureTasks() {
        return ResponseEntity.ok(mixtureTaskService.getAllMixtureTasks());
    }
    
    @GetMapping("/listByType")
    public ResponseEntity<List<MixtureTask>> getMixtureTasksByType(@RequestParam String taskType) {
        return ResponseEntity.ok(mixtureTaskService.getMixtureTasksByType(taskType));
    }
    
    /**
     * 根据任务ID获取项目名称
     * 
     * @param taskId 任务ID
     * @return 项目名称响应
     */
    @GetMapping("/{taskId}/projectName")
    public ResponseEntity<ProjectNameResponse> getProjectNameByTaskId(@PathVariable Long taskId) {
        logger.info("接收到获取任务ID: {} 的项目名称请求", taskId);
        String projectName = mixtureTaskService.getProjectNameByTaskId(taskId);
        logger.info("返回项目名称: {}", projectName);
        return ResponseEntity.ok(new ProjectNameResponse(projectName));
    }
}
