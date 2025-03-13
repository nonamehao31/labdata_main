package com.example.labdata.service;

import com.example.labdata.model.Project;
import com.example.labdata.payload.request.ProjectRequest;
import com.example.labdata.payload.response.ProjectResponse;
import com.example.labdata.repository.ProjectRepository;
import com.example.labdata.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 项目服务类，处理项目相关业务逻辑
 */
@Service
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final JwtTokenProvider tokenProvider;
    
    // 用于跟踪每个公司每天创建的项目数量
    private final ConcurrentHashMap<String, AtomicInteger> projectCountByCompanyPerDay = new ConcurrentHashMap<>();

    @Autowired
    public ProjectService(ProjectRepository projectRepository, JwtTokenProvider tokenProvider) {
        this.projectRepository = projectRepository;
        this.tokenProvider = tokenProvider;
    }

    /**
     * 创建新项目
     *
     * @param projectRequest 项目请求对象
     * @param username 创建者用户名
     * @return 创建的项目响应对象
     */
    @Transactional
    public ProjectResponse createProject(ProjectRequest projectRequest, String username) {
        String companyId = projectRequest.getCompanyId();
        
        Project project = new Project(
                projectRequest.getName(),
                projectRequest.getDescription(),
                projectRequest.getDeadline(),
                companyId,
                username
        );
        
        // 设置项目创建时间
        project.setEstimatedTime(Instant.now());
        
        // 生成项目ID
        String projectId = generateProjectId(companyId);
        project.setProjectId(projectId);
        
        logger.info("创建项目: {}, 项目ID: {}, 公司ID: {}", project.getName(), projectId, companyId);
        
        Project savedProject = projectRepository.save(project);
        return convertToResponse(savedProject);
    }

    /**
     * 生成项目ID
     * 格式: 当前日期+公司ID+序列号
     * 例如: 2025031312301, 2025031312302
     *
     * @param companyId 公司ID
     * @return 生成的项目ID
     */
    private String generateProjectId(String companyId) {
        // 获取当前日期和时间，精确到毫秒
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        // 获取当前时间戳的后6位，增加随机性
        String timeMillisSuffix = String.valueOf(System.currentTimeMillis() % 1000000);
        
        // 创建唯一的键以跟踪每个公司每天的项目数量
        String key = dateStr + companyId;
        
        // 获取并递增该公司今天的项目数量
        AtomicInteger counter = projectCountByCompanyPerDay.computeIfAbsent(key, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();
        
        // 格式化计数器为两位数
        String countStr = String.format("%02d", count);
        
        // 组合项目ID: 日期 + 公司ID + 时间戳后缀 + 计数
        return dateStr + companyId + timeMillisSuffix + countStr;
    }

    /**
     * 获取用户的所有项目
     *
     * @param username 用户名
     * @return 项目响应对象列表
     */
    public List<ProjectResponse> getProjectsByUser(String username) {
        List<Project> projects = projectRepository.findByCreatedBy(username);
        return projects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 获取特定公司的所有项目
     *
     * @param companyId 公司ID
     * @return 项目响应对象列表
     */
    public List<ProjectResponse> getProjectsByCompany(String companyId) {
        List<Project> projects = projectRepository.findByCompanyId(companyId);
        return projects.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取项目
     *
     * @param id 项目ID
     * @return 项目响应对象，如果未找到则返回null
     */
    public ProjectResponse getProjectById(Long id) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        return projectOpt.map(this::convertToResponse).orElse(null);
    }

    /**
     * 根据项目ID获取项目
     *
     * @param projectId 项目ID
     * @return 项目响应对象
     */
    public Optional<ProjectResponse> getProjectById(String projectId) {
        return projectRepository.findByProjectId(projectId)
                .map(this::convertToResponse);
    }
    
    /**
     * 删除项目
     *
     * @param id 项目ID
     * @param username 当前用户名
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteProject(Long id, String username) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            
            // 检查当前用户是否有权限删除该项目（是创建者或同一公司的用户）
            if (project.getCreatedBy().equals(username)) {
                logger.info("用户 {} 删除项目: {}, ID: {}", username, project.getName(), id);
                projectRepository.delete(project);
                return true;
            } else {
                logger.warn("用户 {} 尝试删除项目 {} 失败，权限不足", username, id);
                return false;
            }
        } else {
            logger.warn("用户 {} 尝试删除不存在的项目 {}", username, id);
            return false;
        }
    }
    
    /**
     * 将项目实体转换为响应对象
     *
     * @param project 项目实体
     * @return 项目响应对象
     */
    private ProjectResponse convertToResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectId(project.getProjectId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setDeadline(project.getDeadline());
        response.setCompanyId(project.getCompanyId());
        response.setCreatedBy(project.getCreatedBy());
        response.setCreatedDate(project.getCreatedAt());
        response.setUpdatedDate(project.getUpdatedAt());
        return response;
    }
}
