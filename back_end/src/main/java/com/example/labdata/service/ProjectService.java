package com.example.labdata.service;

import com.example.labdata.exception.ResourceNotFoundException;
import com.example.labdata.model.Project;
import com.example.labdata.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> getAllProjectsByOrganization(Long organizationId) {
        return projectRepository.findByOrganizationId(organizationId);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    public Project getProjectByClientId(Long clientId) {
        return projectRepository.findByClientId(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "clientId", clientId));
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project projectDetails) {
        Project project = getProjectById(id);
        
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        project.setManager(projectDetails.getManager());
        project.setLocation(projectDetails.getLocation());
        project.setStartDate(projectDetails.getStartDate());
        project.setEndDate(projectDetails.getEndDate());
        project.setStatus(projectDetails.getStatus());
        project.setSynced(projectDetails.isSynced());
        
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }
}
