package com.example.labdata.service;

import com.example.labdata.model.AsphaltTask;
import com.example.labdata.payload.request.AsphaltExperimentRequest;
import com.example.labdata.repository.AsphaltTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 沥青实验任务服务类
 */
@Service
public class AsphaltTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltTaskService.class);

    @Autowired
    private AsphaltTaskRepository asphaltTaskRepository;

    /**
     * 创建单个沥青实验任务
     *
     * @param request 沥青实验请求
     * @return 创建的沥青实验任务
     */
    public AsphaltTask createAsphaltExperiment(AsphaltExperimentRequest request) {
        logger.info("创建沥青实验任务: {}", request.getAsphaltExperimentName());
        
        // 获取请求中的参数
        String taskName = request.getAsphaltTaskName();
        if (taskName == null || taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        
        // 添加调试日志 - 检查沥青ID是否存在
        Long selectedAsphaltId = request.getSelectedAsphaltId();
        logger.info("接收到的沥青ID: {}", selectedAsphaltId);
        
        // 前端发送的实验名称实际上是任务分派内容
        String taskAssignment = request.getAsphaltTaskAssignment();
        if (taskAssignment == null || taskAssignment.trim().isEmpty()) {
            // 如果任务分派为空，使用前端传入的实验名称作为任务分派
            taskAssignment = request.getAsphaltExperimentName();
            logger.info("任务分派为空，使用前端发送的实验名称作为任务分派: {}", taskAssignment);
        }
        
        // 将任务名称也用作实验名称，因为experimentName字段是不必要的
        String experimentName = taskName;
        
        // 确保status不为null，给出默认值
        String status = request.getStatus();
        if (status == null || status.trim().isEmpty()) {
            status = "PENDING";
            logger.warn("状态为空，使用默认值: {}", status);
        }
        
        // 确保taskStatus不为null，给出默认值为CREATED
        String taskStatus = request.getTaskStatus();
        if (taskStatus == null || taskStatus.trim().isEmpty()) {
            taskStatus = "CREATED";
            logger.warn("任务状态为空，使用默认值: {}", taskStatus);
        }
        
        AsphaltTask asphaltTask = new AsphaltTask();
        asphaltTask.setAsphaltExperimentName(experimentName);
        asphaltTask.setAsphaltExperimentType(request.getAsphaltExperimentType());
        asphaltTask.setAsphaltTaskName(taskName);
        asphaltTask.setAsphaltTaskAssignment(taskAssignment);
        asphaltTask.setStatus(status);
        asphaltTask.setTaskStatus(taskStatus);
        
        // 设置沥青ID，并记录日志
        asphaltTask.setSelectedAsphaltId(selectedAsphaltId);
        logger.info("设置沥青ID: {}", selectedAsphaltId);
        
        return asphaltTaskRepository.save(asphaltTask);
    }

    /**
     * 批量创建沥青实验任务
     *
     * @param requests 沥青实验请求列表
     * @return 创建的沥青实验任务列表
     */
    public List<AsphaltTask> createAsphaltExperiments(List<AsphaltExperimentRequest> requests) {
        logger.info("批量创建沥青实验任务: {} 条", requests.size());
        
        List<AsphaltTask> asphaltTasks = new ArrayList<>();
        
        for (AsphaltExperimentRequest request : requests) {
            logger.info("处理请求: 实验名称={}, 任务名称={}",
                    request.getAsphaltExperimentName(),
                    request.getAsphaltTaskName());
                    
            // 获取请求中的参数
            String taskName = request.getAsphaltTaskName();
            if (taskName == null || taskName.trim().isEmpty()) {
                throw new IllegalArgumentException("任务名称不能为空");
            }
            
            // 添加调试日志 - 检查沥青ID是否存在
            Long selectedAsphaltId = request.getSelectedAsphaltId();
            logger.info("批量请求中接收到的沥青ID: {}", selectedAsphaltId);
            
            // 前端发送的实验名称实际上是任务分派内容
            String taskAssignment = request.getAsphaltTaskAssignment();
            if (taskAssignment == null || taskAssignment.trim().isEmpty()) {
                // 如果任务分派为空，使用前端传入的实验名称作为任务分派
                taskAssignment = request.getAsphaltExperimentName();
                logger.info("任务分派为空，使用前端发送的实验名称作为任务分派: {}", taskAssignment);
            }
            
            // 将任务名称也用作实验名称，因为experimentName字段是不必要的
            String experimentName = taskName;
            
            // 确保status不为null，给出默认值
            String status = request.getStatus();
            if (status == null || status.trim().isEmpty()) {
                status = "PENDING";
                logger.warn("状态为空，使用默认值: {}", status);
            }
            
            // 确保taskStatus不为null，给出默认值为CREATED
            String taskStatus = request.getTaskStatus();
            if (taskStatus == null || taskStatus.trim().isEmpty()) {
                taskStatus = "CREATED";
                logger.warn("任务状态为空，使用默认值: {}", taskStatus);
            }
            
            AsphaltTask asphaltTask = new AsphaltTask();
            asphaltTask.setAsphaltExperimentName(experimentName);
            asphaltTask.setAsphaltExperimentType(request.getAsphaltExperimentType());
            asphaltTask.setAsphaltTaskName(taskName);
            asphaltTask.setAsphaltTaskAssignment(taskAssignment);
            asphaltTask.setStatus(status);
            asphaltTask.setTaskStatus(taskStatus);
            
            // 设置沥青ID，并记录日志
            asphaltTask.setSelectedAsphaltId(selectedAsphaltId);
            logger.info("为任务 {} 设置沥青ID: {}", taskName, selectedAsphaltId);
            
            asphaltTasks.add(asphaltTask);
        }
        
        return asphaltTaskRepository.saveAll(asphaltTasks);
    }

    /**
     * 获取所有沥青实验任务
     *
     * @return 沥青实验任务列表
     */
    public List<AsphaltTask> getAllAsphaltExperiments() {
        logger.info("获取所有沥青实验任务");
        return asphaltTaskRepository.findAll();
    }

    /**
     * 根据ID获取沥青实验任务
     *
     * @param id 沥青实验任务ID
     * @return 沥青实验任务
     */
    public AsphaltTask getAsphaltExperimentById(Long id) {
        logger.info("根据ID获取沥青实验任务: {}", id);
        return asphaltTaskRepository.findById(id).orElse(null);
    }

    /**
     * 根据类型获取沥青实验任务
     *
     * @param type 沥青实验任务类型
     * @return 沥青实验任务列表
     */
    public List<AsphaltTask> getAsphaltExperimentsByType(String type) {
        logger.info("根据类型获取沥青实验任务: {}", type);
        return asphaltTaskRepository.findByAsphaltExperimentType(type);
    }

    /**
     * 根据名称获取沥青实验任务
     *
     * @param name 沥青实验任务名称
     * @return 沥青实验任务列表
     */
    public List<AsphaltTask> getAsphaltExperimentsByName(String name) {
        logger.info("根据名称获取沥青实验任务: {}", name);
        return asphaltTaskRepository.findByAsphaltExperimentName(name);
    }
}
