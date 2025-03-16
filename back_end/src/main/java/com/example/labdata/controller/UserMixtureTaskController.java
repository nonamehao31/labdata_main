package com.example.labdata.controller;

import com.example.labdata.model.User;
import com.example.labdata.model.UserMixtureTask;
import com.example.labdata.payload.request.UserMixtureTaskRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.repository.UserMixtureTaskRepository;
import com.example.labdata.repository.UserRepository;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/mixture-tasks")
public class UserMixtureTaskController {

    private static final Logger logger = LoggerFactory.getLogger(UserMixtureTaskController.class);

    @Autowired
    private UserMixtureTaskRepository userMixtureTaskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 保存混合料任务
     * @param currentUser 当前用户
     * @param request 任务请求
     * @return 保存结果
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> saveMixtureTask(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody UserMixtureTaskRequest request) {
        
        logger.info("接收到混合料任务保存请求：项目ID={}, 任务名称={}, 配比数量={}, 任务指派数量={}",
                request.getProjectId(), 
                request.getTaskName(),
                request.getMixratioSpecimenPairs() != null ? request.getMixratioSpecimenPairs().size() : 0,
                request.getTaskAssignments() != null ? request.getTaskAssignments().size() : 0);
        
        try {
            // 获取当前用户信息
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", null));
            }
            
            User user = userOpt.get();
            
            // 生成主任务ID，用于关联所有子任务
            String mainTaskId = UUID.randomUUID().toString();
            
            // 保存所有配比-制件方式组合与任务指派的组合
            List<UserMixtureTask> savedTasks = new ArrayList<>();
            int counter = 0; // 用于确保每个任务ID都是唯一的
            
            for (UserMixtureTaskRequest.MixratioSpecimenPair pair : request.getMixratioSpecimenPairs()) {
                // 如果没有任务指派，至少创建一个任务记录
                if (request.getTaskAssignments() == null || request.getTaskAssignments().isEmpty()) {
                    // 使用计数器生成唯一的taskId
                    String taskId = mainTaskId + "-" + (counter++);
                    
                    UserMixtureTask task = UserMixtureTask.createTask(
                            taskId, 
                            user.getOrganizationId(), 
                            user.getId(), 
                            request.getProjectId(),
                            pair.getMixratioId(),
                            pair.getSpecimenId(),
                            null,
                            request.getRemarks(),
                            request.getTaskName()
                    );
                    savedTasks.add(userMixtureTaskRepository.save(task));
                } else {
                    // 为每个任务指派创建一条记录
                    for (String assignment : request.getTaskAssignments()) {
                        // 使用计数器生成唯一的taskId
                        String taskId = mainTaskId + "-" + (counter++);
                        
                        UserMixtureTask task = UserMixtureTask.createTask(
                                taskId, 
                                user.getOrganizationId(), 
                                user.getId(), 
                                request.getProjectId(),
                                pair.getMixratioId(),
                                pair.getSpecimenId(),
                                assignment,
                                request.getRemarks(),
                                request.getTaskName()
                        );
                        savedTasks.add(userMixtureTaskRepository.save(task));
                    }
                }
            }
            
            if (savedTasks.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "没有创建任何任务", null));
            }
            
            logger.info("成功保存混合料任务，主任务ID={}, 总共保存{}条记录", mainTaskId, savedTasks.size());
            return ResponseEntity.ok(new ApiResponse<>(true, "成功创建" + savedTasks.size() + "个混合料任务", mainTaskId));
            
        } catch (Exception e) {
            logger.error("保存混合料任务时发生错误", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "保存失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取当前用户的混合料任务
     * @param currentUser 当前用户
     * @return 用户任务列表
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserMixtureTask>>> getUserMixtureTasks(
            @CurrentUser UserPrincipal currentUser) {
        
        try {
            List<UserMixtureTask> tasks = userMixtureTaskRepository.findByEstBy(currentUser.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取任务成功", tasks));
        } catch (Exception e) {
            logger.error("获取用户混合料任务失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取任务失败: " + e.getMessage(), null));
        }
    }
    
    /**
     * 获取单位的混合料任务
     * @param currentUser 当前用户
     * @return 单位任务列表
     */
    @GetMapping("/company")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<UserMixtureTask>>> getCompanyMixtureTasks(
            @CurrentUser UserPrincipal currentUser) {
        
        try {
            Optional<User> userOpt = userRepository.findById(currentUser.getId());
            if (!userOpt.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(false, "用户不存在", null));
            }
            
            User user = userOpt.get();
            List<UserMixtureTask> tasks = userMixtureTaskRepository.findByTaskCompany(user.getOrganizationId());
            return ResponseEntity.ok(new ApiResponse<>(true, "获取任务成功", tasks));
        } catch (Exception e) {
            logger.error("获取单位混合料任务失败", e);
            return ResponseEntity.ok(new ApiResponse<>(false, "获取任务失败: " + e.getMessage(), null));
        }
    }
}
