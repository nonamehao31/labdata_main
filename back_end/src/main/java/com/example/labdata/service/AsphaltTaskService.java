package com.example.labdata.service;

import com.example.labdata.common.ApiResponse;
import com.example.labdata.model.AsphaltTask;
import com.example.labdata.model.TestAsphaltMaterial;
import com.example.labdata.payload.request.AsphaltExperimentRequest;
import com.example.labdata.payload.response.AsphaltDetailResponse;
import com.example.labdata.repository.AsphaltTaskRepository;
import com.example.labdata.repository.TestAsphaltMaterialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
        asphaltTask.setExperimentStatus("unfinished"); // 设置实验状态为未完成

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

        // 使用正确的方法查询：findByAsphaltTaskAssignmentId
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
     * 接受沥青任务
     *
     * @param taskId 任务ID
     * @param acceptor 接受者
     * @param acceptTime 接受时间
     * @return 更新后的沥青任务
     */
    public AsphaltTask acceptAsphaltTask(String taskId, String acceptor, Long acceptTime) {
        logger.info("接受沥青任务: taskId={}, acceptor={}, acceptTime={}", taskId, acceptor, acceptTime);

        // 查找任务
        List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
        if (tasks.isEmpty()) {
            logger.warn("未找到分配ID为 {} 的沥青实验任务", taskId);
            return null;
        }

        AsphaltTask firstTask = tasks.get(0);
        logger.info("找到 {} 个相关沥青任务，准备全部更新为 ONGOING 状态", tasks.size());

        // 更新所有关联任务的状态
        for (AsphaltTask task : tasks) {
            // 更新任务状态为ONGOING
            task.setTaskStatus("ONGOING");
            task.setStatus("ONGOING");

            // 设置接受人和接受时间
            task.setAcceptor(acceptor);
            task.setAcceptTime(acceptTime);

            // 保存更新后的任务
            updateAsphaltTask(task);
            logger.info("已更新沥青任务状态: ID={}, 名称={}, 新状态=ONGOING",
                    task.getAsphaltExperimentId(), task.getAsphaltTaskName());
        }

        logger.info("沥青任务已被接受: ID={}, 接受人={}", taskId, acceptor);

        return firstTask;
    }

    /**
     * 更新实验任务状态为已完成
     *
     * @param taskId 任务ID
     * @param experimentType 实验类型
     * @return 更新后的任务
     */
    public AsphaltTask updateExperimentStatusToFinished(Long taskId, String experimentType) {
        logger.info("更新实验任务状态为已完成: taskId={}, experimentType={}", taskId, experimentType);

        AsphaltTask task = getAsphaltExperimentById(taskId);
        if (task == null) {
            logger.warn("未找到ID为 {} 的沥青实验任务", taskId);
            return null;
        }

        // 确保实验类型匹配
        if (!task.getAsphaltExperimentType().equals(experimentType)) {
            logger.warn("实验类型不匹配: 请求类型={}, 任务类型={}", experimentType, task.getAsphaltExperimentType());
            return null;
        }

        task.setExperimentStatus("finished");
        logger.info("实验任务状态已更新为已完成: taskId={}", taskId);

        return asphaltTaskRepository.save(task);
    }

    /**
     * 根据ID获取实验状态
     *
     * @param taskId 任务ID
     * @return 实验状态
     */
    public String getExperimentStatus(Long taskId) {
        logger.info("获取实验任务状态: taskId={}", taskId);

        AsphaltTask task = getAsphaltExperimentById(taskId);
        if (task == null) {
            logger.warn("未找到ID为 {} 的沥青实验任务", taskId);
            return null;
        }

        return task.getExperimentStatus();
    }

    /**
     * 更新实验任务状态为已完成（使用字符串ID）
     *
     * @param taskId 任务ID（字符串）
     * @param experimentType 实验类型
     * @return 更新后的任务
     */
    public AsphaltTask updateExperimentStatusToFinishedByStringId(String taskId, String experimentType) {
        logger.info("更新实验任务状态为已完成（使用字符串ID）: taskId={}, experimentType={}", taskId, experimentType);

        // 根据分配ID查找任务
        List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
        if (tasks.isEmpty()) {
            logger.warn("未找到分配ID为 {} 的沥青实验任务", taskId);
            return null;
        }

        // 记录找到的任务信息，用于调试
        logger.info("找到 {} 个任务，详细信息如下:", tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            AsphaltTask task = tasks.get(i);
            logger.info("任务 #{}: ID={}, 名称={}, 实验类型={}, 分配名称={}",
                   i+1, task.getAsphaltExperimentId(), task.getAsphaltTaskName(),
                   task.getAsphaltExperimentType(), task.getAsphaltTaskAssignment());
        }

        // 创建实验类型映射（英文标识符 -> 中文实验名称）
        Map<String, List<String>> experimentTypeMap = new HashMap<>();
        experimentTypeMap.put("penetration", Arrays.asList("针入度试验", "针入度"));
        experimentTypeMap.put("softening_point", Arrays.asList("软化点试验", "软化点", "软化点试验 (环状法)"));
        experimentTypeMap.put("ductility", Arrays.asList("延度试验", "延度"));
        experimentTypeMap.put("brookfield_viscosity", Arrays.asList("沥青旋转黏度试验", "布氏旋转黏度", "布鲁克菲尔德"));
        experimentTypeMap.put("dynamic_shear_rheometer", Arrays.asList("动态剪切流变仪试验", "动态剪切", "动剪"));
        experimentTypeMap.put("bending_beam_rheometer", Arrays.asList("沥青弯曲蠕变劲度试验", "BBR试验", "弯曲梁"));
        experimentTypeMap.put("bbr", experimentTypeMap.get("bending_beam_rheometer")); // 添加"bbr"作为bending_beam_rheometer的别名

        // 获取要查找的中文实验类型列表
        List<String> targetTypesList = experimentTypeMap.get(experimentType);
        if (targetTypesList == null) {
            logger.warn("未在映射表中找到实验类型 {} 的对应中文名称", experimentType);
            targetTypesList = Collections.singletonList(experimentType);
        } else {
            logger.info("实验类型 {} 对应的中文名称列表: {}", experimentType, targetTypesList);
        }

        AsphaltTask taskToUpdate = null;

        // 使用asphalt_task_assignment字段进行匹配
        for (AsphaltTask task : tasks) {
            // 检查asphalt_task_assignment是否为空
            String assignmentName = task.getAsphaltTaskAssignment();
            if (assignmentName == null || assignmentName.isEmpty()) {
                continue;
            }

            // 检查assignment是否包含映射表中的任何一个中文名称
            for (String targetType : targetTypesList) {
                if (assignmentName.contains(targetType)) {
                    taskToUpdate = task;
                    logger.info("成功匹配: 任务分配名称 [{}] 包含目标类型 [{}]", assignmentName, targetType);
                    break;
                }
            }

            if (taskToUpdate != null) {
                break;
            }
        }

        // 如果asphalt_task_assignment匹配失败，尝试使用experiment_type匹配
        if (taskToUpdate == null) {
            for (AsphaltTask task : tasks) {
                String taskType = task.getAsphaltExperimentType();

                // 检查任务类型是否包含映射表中的任何一个中文名称
                for (String targetType : targetTypesList) {
                    if (taskType != null && taskType.contains(targetType)) {
                        taskToUpdate = task;
                        logger.info("成功匹配: 任务类型 [{}] 包含目标类型 [{}]", taskType, targetType);
                        break;
                    }
                }

                if (taskToUpdate != null) {
                    break;
                }
            }
        }

        // 如果没找到匹配的任务，使用第一个
        if (taskToUpdate == null && !tasks.isEmpty()) {
            taskToUpdate = tasks.get(0);
            logger.warn("未找到与实验类型 {} 匹配的任务，使用第一个找到的任务: ID={}, 名称={}, 分配名称={}",
                   experimentType, taskToUpdate.getAsphaltExperimentId(),
                   taskToUpdate.getAsphaltTaskName(), taskToUpdate.getAsphaltTaskAssignment());
        }

        if (taskToUpdate != null) {
            taskToUpdate.setExperimentStatus("finished");
            logger.info("实验任务状态已更新为已完成: taskId={}, 任务名称={}, 分配名称={}, 实验类型={}",
                   taskToUpdate.getAsphaltExperimentId(), taskToUpdate.getAsphaltTaskName(),
                   taskToUpdate.getAsphaltTaskAssignment(), experimentType);
            
            // 保存当前更新的任务
            AsphaltTask savedTask = asphaltTaskRepository.save(taskToUpdate);
            
            // 检查该任务组中的所有实验是否全部完成
            boolean allExperimentsFinished = true;
            for (AsphaltTask task : tasks) {
                // 重新从数据库获取最新状态
                Optional<AsphaltTask> refreshedTask = asphaltTaskRepository.findById(task.getAsphaltExperimentId());
                if (refreshedTask.isPresent() && !"finished".equalsIgnoreCase(refreshedTask.get().getExperimentStatus())) {
                    allExperimentsFinished = false;
                    logger.info("任务组中还有未完成的实验: taskId={}, 名称={}, 状态={}",
                            task.getAsphaltExperimentId(), task.getAsphaltTaskName(), refreshedTask.get().getExperimentStatus());
                    break;
                }
            }
            
            // 如果所有实验都已完成，更新整个任务组的状态
            if (allExperimentsFinished) {
                logger.info("任务组中所有实验均已完成，更新整个任务组状态为COMPLETED");
                for (AsphaltTask task : tasks) {
                    task.setTaskStatus("COMPLETED");
                    asphaltTaskRepository.save(task);
                    logger.info("更新任务状态为COMPLETED: taskId={}, 名称={}",
                            task.getAsphaltExperimentId(), task.getAsphaltTaskName());
                }
            }
            
            return savedTask;
        }

        return null;
    }

    /**
     * 根据ID获取实验状态
     *
     * @param taskId 任务ID
     * @return 实验状态
     */
    public String getExperimentStatusByStringId(String taskId) {
        logger.info("获取实验任务状态（使用字符串ID）: taskId={}", taskId);

        List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
        if (tasks.isEmpty()) {
            logger.warn("未找到分配ID为 {} 的沥青实验任务", taskId);
            return null;
        }

        // 如果任务组中有任务，返回第一个任务的状态
        AsphaltTask task = tasks.get(0);
        logger.info("获取到任务状态: taskId={}, 状态={}", taskId, task.getExperimentStatus());
        return task.getExperimentStatus();
    }

    /**
     * 根据任务ID获取所有实验类型的状态
     *
     * @param taskId 任务ID（字符串）
     * @return 实验类型到状态的映射
     */
    public Map<String, String> getExperimentTypeStatusByStringId(String taskId) {
        logger.info("获取实验类型状态（使用字符串ID）: taskId={}", taskId);

        List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
        if (tasks.isEmpty()) {
            logger.warn("未找到分配ID为 {} 的沥青实验任务", taskId);
            return new HashMap<>();
        }

        // 创建实验类型映射（中文实验名称 -> 英文标识符）
        Map<String, String> experimentTypeMapReverse = new HashMap<>();
        experimentTypeMapReverse.put("针入度试验", "penetration");
        experimentTypeMapReverse.put("针入度", "penetration");
        experimentTypeMapReverse.put("软化点试验", "softening_point");
        experimentTypeMapReverse.put("软化点", "softening_point");
        experimentTypeMapReverse.put("软化点试验 (环状法)", "softening_point");
        experimentTypeMapReverse.put("延度试验", "ductility");
        experimentTypeMapReverse.put("延度", "ductility");
        experimentTypeMapReverse.put("沥青旋转黏度试验", "brookfield_viscosity");
        experimentTypeMapReverse.put("布氏旋转黏度", "brookfield_viscosity");
        experimentTypeMapReverse.put("布鲁克菲尔德", "brookfield_viscosity");
        experimentTypeMapReverse.put("动态剪切流变仪试验", "dynamic_shear_rheometer");
        experimentTypeMapReverse.put("动态剪切", "dynamic_shear_rheometer");
        experimentTypeMapReverse.put("动剪", "dynamic_shear_rheometer");
        experimentTypeMapReverse.put("沥青弯曲蠕变劲度试验", "bending_beam_rheometer");
        experimentTypeMapReverse.put("BBR试验", "bending_beam_rheometer");
        experimentTypeMapReverse.put("弯曲梁", "bending_beam_rheometer");

        // 创建结果映射
        Map<String, String> result = new HashMap<>();
        
        // 针对每个任务，获取实验类型和状态
        for (AsphaltTask task : tasks) {
            String assignmentName = task.getAsphaltTaskAssignment();
            String experimentStatus = task.getExperimentStatus();
            
            if (assignmentName == null || assignmentName.isEmpty() || experimentStatus == null) {
                continue;
            }
            
            // 尝试从任务分配名称中提取实验类型
            String experimentTypeKey = null;
            for (Map.Entry<String, String> entry : experimentTypeMapReverse.entrySet()) {
                if (assignmentName.contains(entry.getKey())) {
                    experimentTypeKey = entry.getValue();
                    break;
                }
            }
            
            // 如果找到了实验类型，添加到结果映射
            if (experimentTypeKey != null) {
                logger.info("实验类型状态: 类型={}, 状态={}", experimentTypeKey, experimentStatus);
                result.put(experimentTypeKey, experimentStatus);
            } else {
                // 如果没有找到，尝试使用整个分配名称作为键
                logger.info("未识别的实验类型: 名称={}, 状态={}", assignmentName, experimentStatus);
                result.put(assignmentName, experimentStatus);
            }
        }
        
        logger.info("获取到 {} 个实验类型状态", result.size());
        return result;
    }

    /**
     * 根据任务ID更新实验设备信息
     * @param taskId 任务ID字符串（可能是asphalt_task_assignment_id）
     * @param equipment 设备型号
     * @param manufacturer 设备厂家
     * @return 更新后的任务
     */
    public AsphaltTask updateEquipmentInfo(String taskId, String equipment, String manufacturer) {
        logger.info("更新实验设备信息: taskId={}, equipment={}, manufacturer={}", 
                taskId, equipment, manufacturer);
        
        // 根据分配ID查找任务
        List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
        if (tasks.isEmpty()) {
            logger.warn("未找到分配ID为 {} 的沥青实验任务", taskId);
            
            // 尝试使用任务ID直接查找
            try {
                Long numericId = Long.parseLong(taskId);
                Optional<AsphaltTask> taskOpt = asphaltTaskRepository.findById(numericId);
                if (taskOpt.isPresent()) {
                    AsphaltTask task = taskOpt.get();
                    task.setAssignedAsphaltEquipment(equipment);
                    task.setAssignedAsphaltEquipmentManufacturer(manufacturer);
                    task.setUpdatedAt(Instant.now());
                    
                    AsphaltTask updatedTask = asphaltTaskRepository.save(task);
                    logger.info("已使用数字ID更新沥青任务的设备信息: ID={}, 设备={}, 厂家={}", 
                            numericId, equipment, manufacturer);
                    return updatedTask;
                }
            } catch (NumberFormatException e) {
                logger.warn("任务ID {} 无法转换为数字", taskId);
            }
            
            return null;
        }
        
        // 更新找到的第一个任务的设备信息
        AsphaltTask task = tasks.get(0);
        task.setAssignedAsphaltEquipment(equipment);
        task.setAssignedAsphaltEquipmentManufacturer(manufacturer);
        task.setUpdatedAt(Instant.now());
        
        AsphaltTask updatedTask = asphaltTaskRepository.save(task);
        logger.info("已更新沥青任务的设备信息: ID={}, 设备={}, 厂家={}", 
                task.getAsphaltExperimentId(), equipment, manufacturer);
        
        // 如果有多个相关任务，也一并更新它们的设备信息
        if (tasks.size() > 1) {
            logger.info("发现多个关联任务 ({} 个)，正在更新所有任务的设备信息", tasks.size());
            
            for (int i = 1; i < tasks.size(); i++) {
                AsphaltTask relatedTask = tasks.get(i);
                relatedTask.setAssignedAsphaltEquipment(equipment);
                relatedTask.setAssignedAsphaltEquipmentManufacturer(manufacturer);
                relatedTask.setUpdatedAt(Instant.now());
                
                asphaltTaskRepository.save(relatedTask);
                logger.info("已更新关联沥青任务的设备信息: ID={}", relatedTask.getAsphaltExperimentId());
            }
        }
        
        return updatedTask;
    }

    /**
     * 获取任务指派信息和设备信息
     * 
     * @param asphaltExperimentId 沥青实验ID
     * @return 包含任务指派信息和设备信息的Map
     */
    public Map<String, String> getTaskAssignmentAndEquipment(String asphaltExperimentId) {
        if (asphaltExperimentId == null || asphaltExperimentId.isEmpty()) {
            logger.error("沥青实验ID为空，无法获取任务指派信息和设备信息");
            return null;
        }

        try {
            // 使用JDBC参数化查询获取任务指派信息和设备信息
            String sql = "SELECT asphalt_task_assignment, " +
                    "assigned_asphalt_equipment, assigned_asphalt_equipment_manufacturer " +
                    "FROM asphalt_task WHERE asphalt_experiment_id = ?";
            
            Object[] params = new Object[]{asphaltExperimentId};
            int[] types = new int[]{java.sql.Types.VARCHAR}; // 明确指定参数类型为VARCHAR

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params, types);

            if (!results.isEmpty()) {
                Map<String, String> taskInfo = new HashMap<>();
                
                // 提取任务指派信息
                Object taskAssignment = results.get(0).get("asphalt_task_assignment");
                if (taskAssignment != null) {
                    taskInfo.put("taskAssignment", taskAssignment.toString());
                }
                
                // 提取设备信息
                Object assignedAsphaltEquipment = results.get(0).get("assigned_asphalt_equipment");
                if (assignedAsphaltEquipment != null) {
                    taskInfo.put("assignedAsphaltEquipment", assignedAsphaltEquipment.toString());
                }
                
                Object asphaltEquipmentManufacturer = results.get(0).get("assigned_asphalt_equipment_manufacturer");
                if (asphaltEquipmentManufacturer != null) {
                    taskInfo.put("asphaltEquipmentManufacturer", asphaltEquipmentManufacturer.toString());
                }
                
                return taskInfo;
            }
            
            return null;
        } catch (Exception e) {
            logger.error("获取任务指派信息和设备信息时发生错误: {}", e.getMessage(), e);
            return null;
        }
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

    @GetMapping("/experimentStatus/{taskId}")
    public ApiResponse<Map<String, String>> getExperimentStatus(@PathVariable String taskId) {
        try {
            // 记录请求
            logger.info("获取实验状态请求, 任务ID: {}", taskId);
            
            // 确保taskId不为空
            if (taskId == null || taskId.trim().isEmpty()) {
                logger.warn("获取实验状态失败: 任务ID为空");
                return new ApiResponse<>(false, "任务ID不能为空", new HashMap<>()); // 返回空Map而非null
            }
            
            // 查找任务 - 使用分配ID查找所有相关任务
            List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
            if (tasks.isEmpty()) {
                logger.warn("未找到分配ID为 {} 的任务", taskId);
                return new ApiResponse<>(false, "未找到ID为 " + taskId + " 的任务", new HashMap<>()); // 返回空Map而非null
            }
            
            // 创建实验类型映射（英文标识符 -> 中文实验名称列表）
            Map<String, List<String>> experimentTypeMap = new HashMap<>();
            experimentTypeMap.put("penetration", Arrays.asList("针入度试验", "针入度"));
            experimentTypeMap.put("softening_point", Arrays.asList("软化点试验", "软化点", "软化点试验 (环状法)"));
            experimentTypeMap.put("ductility", Arrays.asList("延度试验", "延度"));
            experimentTypeMap.put("brookfield_viscosity", Arrays.asList("沥青旋转黏度试验", "布氏旋转黏度", "布鲁克菲尔德"));
            experimentTypeMap.put("dynamic_shear_rheometer", Arrays.asList("动态剪切流变仪试验", "动态剪切", "动剪"));
            experimentTypeMap.put("bending_beam_rheometer", Arrays.asList("沥青弯曲蠕变劲度试验", "BBR试验", "弯曲梁"));
            experimentTypeMap.put("bbr", experimentTypeMap.get("bending_beam_rheometer")); // 添加"bbr"作为bending_beam_rheometer的别名
            
            // 初始化状态映射 - 默认所有类型为未完成
            Map<String, String> experimentStatusMap = new HashMap<>();
            experimentStatusMap.put("软化点试验", "unfinished");
            experimentStatusMap.put("针入度试验", "unfinished");
            experimentStatusMap.put("延度试验", "unfinished");
            experimentStatusMap.put("布氏旋转黏度", "unfinished");
            experimentStatusMap.put("动态剪切流变仪", "unfinished");
            experimentStatusMap.put("沥青弯曲蠕变劲度", "unfinished");
            
            // 检查每个任务的assignment和status
            logger.info("找到 {} 个相关任务，开始分析每个任务的实验类型和状态", tasks.size());
            for (AsphaltTask task : tasks) {
                String assignment = task.getAsphaltTaskAssignment();
                String status = task.getExperimentStatus();
                
                if (assignment == null) {
                    logger.warn("任务 {} 的分配名称为空", task.getAsphaltExperimentId());
                    continue; // 跳过空分配名称的任务
                }
                
                logger.info("任务详情: ID={}, 分配名称={}, 状态={}", 
                           task.getAsphaltExperimentId(), assignment, status);
                
                // 如果任务状态为已完成，查找匹配的实验类型并更新状态
                if ("finished".equals(status) && !assignment.isEmpty()) {
                    boolean matched = false;
                    
                    // 遍历所有已知实验类型，检查assignment是否包含该类型
                    for (Map.Entry<String, List<String>> entry : experimentTypeMap.entrySet()) {
                        for (String typeName : entry.getValue()) {
                            if (assignment.contains(typeName)) {
                                // 找到匹配的类型，更新状态映射
                                for (String key : experimentStatusMap.keySet()) {
                                    if (key.contains(typeName) || typeName.contains(key)) {
                                        experimentStatusMap.put(key, "finished");
                                        logger.info("将实验类型 [{}] 状态设置为已完成", key);
                                        matched = true;
                                    }
                                }
                                
                                if (!matched) {
                                    // 如果在状态映射中没有找到匹配的键，直接添加
                                    experimentStatusMap.put(typeName, "finished");
                                    logger.info("添加新的实验类型 [{}] 状态为已完成", typeName);
                                }
                                
                                break;
                            }
                        }
                        if (matched) break;
                    }
                    
                    if (!matched) {
                        logger.warn("任务 [{}] 状态为已完成，但未能识别实验类型: {}", 
                                  task.getAsphaltExperimentId(), assignment);
                    }
                }
            }
            
            // 记录返回数据
            logger.info("返回任务 {} 的实验状态映射: {}", taskId, experimentStatusMap);
            
            // 确保返回的是Map对象而不是字符串
            return new ApiResponse<>(true, "成功获取实验状态", experimentStatusMap);
        } catch (Exception e) {
            logger.error("获取实验状态出错: ", e);
            // 发生异常时也返回空Map而不是null或字符串
            return new ApiResponse<>(false, "获取实验状态时发生错误: " + e.getMessage(), new HashMap<>());
        }
    }
    
    /**
     * 更新特定实验类型的状态为已完成
     *
     * @param taskId 任务ID（字符串）
     * @param experimentType 实验类型
     * @return 成功或失败的响应
     */
    @GetMapping("/updateExperimentType/{taskId}/{experimentType}")
    public ApiResponse<Boolean> updateExperimentTypeStatusToFinished(@PathVariable String taskId, 
                                                                    @PathVariable String experimentType) {
        try {
            logger.info("更新特定实验类型状态请求, 任务ID: {}, 实验类型: {}", taskId, experimentType);
            
            // 调用现有方法更新状态
            AsphaltTask updatedTask = updateExperimentStatusToFinishedByStringId(taskId, experimentType);
            
            if (updatedTask != null) {
                logger.info("成功更新实验 {} 的状态为已完成", experimentType);
                return new ApiResponse<>(true, "成功更新实验状态", Boolean.TRUE);
            } else {
                logger.warn("更新实验状态失败，未找到匹配的任务或实验类型");
                return new ApiResponse<>(false, "更新实验状态失败，未找到匹配的任务或实验类型", Boolean.FALSE);
            }
        } catch (Exception e) {
            logger.error("更新实验状态时发生错误: ", e);
            return new ApiResponse<>(false, "更新实验状态时发生错误: " + e.getMessage(), Boolean.FALSE);
        }
    }
    
    /**
     * 更新整个实验的状态
     *
     * @param taskId 任务ID（字符串）
     * @param status 要设置的状态（如果为null则设为已完成）
     * @return 成功或失败的响应
     */
    @GetMapping("/updateExperimentStatus/{taskId}")
    public ApiResponse<Boolean> updateExperimentStatus(@PathVariable String taskId, 
                                                     @RequestParam(required = false) String status) {
        try {
            logger.info("更新整个实验状态请求, 任务ID: {}, 状态: {}", taskId, status != null ? status : "finished");
            
            // 查找任务 
            List<AsphaltTask> tasks = getAsphaltTasksByAssignmentId(taskId);
            if (tasks.isEmpty()) {
                logger.warn("未找到分配ID为 {} 的任务", taskId);
                return new ApiResponse<>(false, "未找到ID为 " + taskId + " 的任务", Boolean.FALSE);
            }
            
            // 状态默认为已完成
            String newStatus = (status != null) ? status : "finished";
            
            // 更新所有相关任务的状态
            boolean allSuccess = true;
            for (AsphaltTask task : tasks) {
                task.setExperimentStatus(newStatus);
                try {
                    asphaltTaskRepository.save(task);
                    logger.info("成功更新任务 {} 的状态为 {}", task.getAsphaltExperimentId(), newStatus);
                } catch (Exception e) {
                    logger.error("更新任务 {} 状态时出错: {}", task.getAsphaltExperimentId(), e.getMessage());
                    allSuccess = false;
                }
            }
            
            if (allSuccess) {
                return new ApiResponse<>(true, "成功更新所有任务状态", Boolean.TRUE);
            } else {
                return new ApiResponse<>(true, "部分任务状态更新失败", Boolean.TRUE);
            }
        } catch (Exception e) {
            logger.error("更新实验状态时发生错误: ", e);
            return new ApiResponse<>(false, "更新实验状态时发生错误: " + e.getMessage(), Boolean.FALSE);
        }
    }
}
