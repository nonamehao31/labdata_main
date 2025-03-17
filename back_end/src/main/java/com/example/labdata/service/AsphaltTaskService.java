package com.example.labdata.service;

import com.example.labdata.model.AsphaltTask;
import com.example.labdata.model.TestAsphaltMaterial;
import com.example.labdata.payload.request.AsphaltExperimentRequest;
import com.example.labdata.payload.response.AsphaltDetailResponse;
import com.example.labdata.repository.AsphaltTaskRepository;
import com.example.labdata.repository.TestAsphaltMaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 沥青实验任务服务类
 */
@Service
public class AsphaltTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltTaskService.class);

    @Autowired
    private AsphaltTaskRepository asphaltTaskRepository;
    
    @Autowired
    private TestAsphaltMaterialRepository testAsphaltMaterialRepository;

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
        
        // 生成统一的asphalt_task_assignment_id
        String assignmentId = request.getAsphaltTaskAssignmentId();
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            // 如果未提供分配ID，则生成一个新的UUID
            assignmentId = generateAsphaltTaskAssignmentId();
            logger.info("生成新的任务分配ID: {}", assignmentId);
        } else {
            logger.info("使用提供的任务分配ID: {}", assignmentId);
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
        asphaltTask.setAsphaltTaskAssignmentId(assignmentId);  // 设置任务分配ID
        asphaltTask.setStatus(status);
        asphaltTask.setTaskStatus(taskStatus);
        
        // 设置沥青ID，并记录日志
        asphaltTask.setSelectedAsphaltId(selectedAsphaltId);
        logger.info("设置沥青ID: {}", selectedAsphaltId);
        
        // 设置公司ID
        String companyId = request.getCompanyId();
        if (companyId != null && !companyId.trim().isEmpty()) {
            asphaltTask.setCompanyId(companyId);
            logger.info("设置公司ID: {}", companyId);
        } else {
            logger.info("未提供公司ID");
        }
        
        // 从test_asphalt_material中获取截止日期
        if (selectedAsphaltId != null) {
            Optional<TestAsphaltMaterial> asphaltMaterial = testAsphaltMaterialRepository.findById(selectedAsphaltId);
            if (asphaltMaterial.isPresent()) {
                asphaltTask.setDueDate(asphaltMaterial.get().getAsphaltTestDue());
                logger.info("从test_asphalt_material中获取并设置截止日期: {}", asphaltMaterial.get().getAsphaltTestDue());
            } else {
                logger.warn("未找到ID为 {} 的沥青材料，无法设置截止日期", selectedAsphaltId);
            }
        } else {
            logger.warn("未提供沥青ID，无法设置截止日期");
        }
        
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
        
        // 为整个批次生成一个统一的任务分配ID
        String batchAssignmentId = generateAsphaltTaskAssignmentId();
        logger.info("为整个批次生成统一的任务分配ID: {}", batchAssignmentId);
        
        List<AsphaltTask> result = new ArrayList<>();
        
        for (AsphaltExperimentRequest request : requests) {
            try {
                // 在请求中设置统一的任务分配ID
                request.setAsphaltTaskAssignmentId(batchAssignmentId);
                
                AsphaltTask task = createAsphaltExperiment(request);
                result.add(task);
                logger.info("成功创建沥青实验任务: {}, 分配ID: {}", task.getAsphaltTaskName(), task.getAsphaltTaskAssignmentId());
            } catch (Exception e) {
                logger.error("创建沥青实验任务失败: {}, 错误: {}", request.getAsphaltTaskName(), e.getMessage());
            }
        }
        
        return result;
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

    /**
     * 根据任务ID获取沥青任务详情信息
     * 
     * @param taskId 任务ID
     * @return 沥青任务详情响应
     */
    public AsphaltDetailResponse getAsphaltDetailByTaskId(String taskId) {
        logger.info("获取任务ID为{}的沥青任务详情", taskId);
        
        List<AsphaltTask> asphaltTasks = asphaltTaskRepository.findByAsphaltTaskAssignmentId(taskId);
        if (asphaltTasks.isEmpty()) {
            logger.warn("未找到任务ID为{}的沥青任务", taskId);
            return new AsphaltDetailResponse(new ArrayList<>(), new HashMap<>());
        }
        
        // 1. 提取所有沥青ID，去重
        Set<Long> asphaltIds = asphaltTasks.stream()
                .map(AsphaltTask::getSelectedAsphaltId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        // 2. 获取沥青信息
        List<AsphaltDetailResponse.AsphaltInfo> asphaltInfoList = new ArrayList<>();
        for (Long asphaltId : asphaltIds) {
            Optional<TestAsphaltMaterial> asphaltMaterial = testAsphaltMaterialRepository.findById(asphaltId);
            if (asphaltMaterial.isPresent()) {
                TestAsphaltMaterial material = asphaltMaterial.get();
                asphaltInfoList.add(new AsphaltDetailResponse.AsphaltInfo(
                        asphaltId,
                        material.getAsphaltSupplier(),
                        material.getAsphaltGrade(),
                        material.getAsphaltCatalog()
                ));
            }
        }
        
        // 3. 获取实验指派信息
        Map<Long, List<String>> experimentAssignments = new HashMap<>();
        for (AsphaltTask task : asphaltTasks) {
            Long asphaltId = task.getSelectedAsphaltId();
            if (asphaltId != null) {
                String assignment = task.getAsphaltTaskAssignment();
                if (assignment != null && !assignment.isEmpty()) {
                    experimentAssignments.computeIfAbsent(asphaltId, k -> new ArrayList<>())
                            .add(assignment);
                }
            }
        }
        
        return new AsphaltDetailResponse(asphaltInfoList, experimentAssignments);
    }

    /**
     * 根据任务分配ID获取沥青任务
     *
     * @param taskId 任务分配ID
     * @return 沥青任务列表
     */
    public List<AsphaltTask> getAsphaltTasksByAssignmentId(String taskId) {
        logger.info("根据任务分配ID获取沥青任务: {}", taskId);
        return asphaltTaskRepository.findByAsphaltTaskAssignmentId(taskId);
    }
    
    /**
     * 更新沥青任务
     *
     * @param task 沥青任务
     * @return 更新后的沥青任务
     */
    public AsphaltTask updateAsphaltTask(AsphaltTask task) {
        logger.info("更新沥青任务: ID={}, 名称={}, 状态={}", 
                task.getAsphaltExperimentId(), 
                task.getAsphaltTaskName(), 
                task.getTaskStatus());
        
        return asphaltTaskRepository.save(task);
    }

    /**
     * 生成沥青任务分配ID
     * 
     * @return 生成的分配ID
     */
    private String generateAsphaltTaskAssignmentId() {
        // 生成基于UUID的任务分配ID
        return UUID.randomUUID().toString();
    }
}
