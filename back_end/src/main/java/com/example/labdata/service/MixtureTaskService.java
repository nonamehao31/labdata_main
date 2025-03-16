package com.example.labdata.service;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.model.Project;
import com.example.labdata.model.UserMixtureTask;
import com.example.labdata.repository.MixtureTaskRepository;
import com.example.labdata.repository.ProjectRepository;
import com.example.labdata.repository.UserMixtureTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MixtureTaskService {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskService.class);

    @Autowired
    private MixtureTaskRepository mixtureTaskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private UserMixtureTaskRepository userMixtureTaskRepository;

    public List<MixtureTask> getAllMixtureTasks() {
        List<MixtureTask> tasks = mixtureTaskRepository.findAll();
        // 查询并设置每个任务的项目名称
        setProjectNamesForTasks(tasks);
        return tasks;
    }
    
    public List<MixtureTask> getMixtureTasksByType(String taskType) {
        List<MixtureTask> tasks = mixtureTaskRepository.findByTaskType(taskType);
        // 查询并设置每个任务的项目名称
        setProjectNamesForTasks(tasks);
        return tasks;
    }
    
    /**
     * 根据任务ID获取项目名称
     * 
     * @param taskId 任务ID
     * @return 项目名称，如果没有找到则返回"未知项目"
     */
    public String getProjectNameByTaskId(Long taskId) {
        logger.info("获取任务ID: {} 的项目名称", taskId);
        
        // 从 mixture_task 表中通过 id 列查找记录
        Optional<UserMixtureTask> userTaskOpt = userMixtureTaskRepository.findById(taskId);
        if (userTaskOpt.isPresent()) {
            UserMixtureTask userTask = userTaskOpt.get();
            logger.info("在mixture_task表中找到任务(id={}): {}，项目ID: {}", 
                        taskId, userTask.getTaskName(), userTask.getProjectId());
            
            if (userTask.getProjectId() != null) {
                // 使用project_id到projects表中查询项目
                Optional<Project> projectOpt = projectRepository.findByProjectId(userTask.getProjectId());
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    logger.info("找到项目: {}", project.getName());
                    return project.getName();
                } else {
                    logger.info("在projects表中未找到project_id={}的项目", userTask.getProjectId());
                }
            } else {
                logger.info("任务的项目ID为null");
            }
        } else {
            logger.info("在mixture_task表中未找到ID: {}的任务", taskId);
        }
        
        return "未知项目";
    }
    
    /**
     * 为任务列表设置项目名称
     * 
     * @param tasks 任务列表
     */
    private void setProjectNamesForTasks(List<MixtureTask> tasks) {
        for (MixtureTask task : tasks) {
            if (task.getProjectId() != null) {
                Optional<Project> projectOpt = projectRepository.findById(task.getProjectId());
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    task.setProject(project);
                    task.setProjectName(project.getName());
                } else {
                    // 如果找不到项目，设置一个默认名称
                    task.setProjectName("未知项目");
                }
            } else {
                task.setProjectName("未分配项目");
            }
        }
    }
}
