package com.example.labdata.controller;

import com.example.labdata.payload.request.ProjectRequest;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.payload.response.ProjectNameResponse;
import com.example.labdata.payload.response.ProjectResponse;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.ProjectService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 项目控制器，处理与项目相关的HTTP请求
 */
@RestController
@RequestMapping("/api")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 创建新项目
     *
     * @param projectRequest 项目请求对象
     * @param currentUser    当前用户
     * @return 创建的项目响应
     */
    @PostMapping("/projects")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody ProjectRequest projectRequest,
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("收到创建项目请求: {} 来自用户: {}", projectRequest.getName(), currentUser.getUsername());
        
        ProjectResponse projectResponse = projectService.createProject(
                projectRequest, 
                currentUser.getUsername()  
        );
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "项目创建成功", projectResponse)
        );
    }

    /**
     * 获取当前用户的所有项目
     *
     * @param currentUser 当前用户
     * @return 项目列表
     */
    @GetMapping("/projects")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjects(
            @CurrentUser UserPrincipal currentUser) {
        
        List<ProjectResponse> projects = projectService.getProjectsByUser(
                currentUser.getUsername()  
        );
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "获取项目列表成功", projects)
        );
    }

    /**
     * 根据ID获取项目详情
     *
     * @param id          项目ID
     * @param currentUser 当前用户
     * @return 项目详情
     */
    @GetMapping("/projects/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        ProjectResponse project = projectService.getProjectById(id);
        
        if (project == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "获取项目详情成功", project)
        );
    }

    /**
     * 获取特定公司的所有项目
     *
     * @param companyId   公司ID
     * @param currentUser 当前用户
     * @return 项目列表
     */
    @GetMapping("/company/{companyId}/projects")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getProjectsByCompany(
            @PathVariable String companyId,
            @CurrentUser UserPrincipal currentUser) {
        
        List<ProjectResponse> projects = projectService.getProjectsByCompany(companyId);
        
        return ResponseEntity.ok(
                new ApiResponse<>(true, "获取公司项目列表成功", projects)
        );
    }
    
    /**
     * 删除项目
     *
     * @param id          项目ID
     * @param currentUser 当前用户
     * @return 删除结果
     */
    @DeleteMapping("/projects/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> deleteProject(
            @PathVariable Long id,
            @CurrentUser UserPrincipal currentUser) {
        
        logger.info("收到删除项目请求: {} 来自用户: {}", id, currentUser.getUsername());
        
        boolean result = projectService.deleteProject(id, currentUser.getUsername());
        
        if (result) {
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "项目删除成功", true)
            );
        } else {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, "项目删除失败，可能是权限不足或项目不存在", false)
            );
        }
    }

    /**
     * 通过ID获取项目名称
     */
    @GetMapping("/projects/{projectId}/name")
    public ResponseEntity<ApiResponse<ProjectNameResponse>> getProjectNameById(@PathVariable Long projectId) {
        logger.info("API请求: 通过ID获取项目名称, projectId={}", projectId);
        
        ProjectResponse project = projectService.getProjectById(projectId);
        
        if (project != null) {
            String projectName = project.getName();
            logger.info("找到项目: projectId={}, projectName={}", projectId, projectName);
            
            ProjectNameResponse response = new ProjectNameResponse(projectId, projectName);
            return ResponseEntity.ok(new ApiResponse<>(true, "成功获取项目名称", response));
        } else {
            logger.warn("未找到项目: projectId={}", projectId);
            ProjectNameResponse response = new ProjectNameResponse(projectId, "未知项目");
            return ResponseEntity.ok(new ApiResponse<>(false, "未找到指定ID的项目", response));
        }
    }
}
