package com.example.labdata.controller;

import com.example.labdata.model.Project;
import com.example.labdata.security.CurrentUser;
import com.example.labdata.security.UserPrincipal;
import com.example.labdata.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<Project> getProjectsByOrganization(@CurrentUser UserPrincipal currentUser) {
        // 获取当前用户所在组织的项目列表
        return projectService.getAllProjectsByOrganization(currentUser.getOrganizationId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Project project = projectService.getProjectById(id);
        
        // 检查是否属于当前用户所在组织
        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(project);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public Project createProject(@Valid @RequestBody Project project, @CurrentUser UserPrincipal currentUser) {
        // 设置组织ID为当前用户所在组织
        project.setOrganizationId(currentUser.getOrganizationId());
        return projectService.createProject(project);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @Valid @RequestBody Project projectDetails, @CurrentUser UserPrincipal currentUser) {
        Project existingProject = projectService.getProjectById(id);
        
        // 检查是否属于当前用户所在组织
        if (!existingProject.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        // 确保不修改组织ID
        projectDetails.setOrganizationId(currentUser.getOrganizationId());
        
        Project updatedProject = projectService.updateProject(id, projectDetails);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteProject(@PathVariable Long id, @CurrentUser UserPrincipal currentUser) {
        Project project = projectService.getProjectById(id);
        
        // 检查是否属于当前用户所在组织
        if (!project.getOrganizationId().equals(currentUser.getOrganizationId())) {
            return ResponseEntity.notFound().build();
        }
        
        projectService.deleteProject(id);
        return ResponseEntity.ok().build();
    }
}
