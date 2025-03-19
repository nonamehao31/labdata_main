package com.example.labdata.service;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioAsphalt;
import com.example.labdata.model.MixRatioSand;
import com.example.labdata.model.MixRatioStone;
import com.example.labdata.model.Project;
import com.example.labdata.model.UserMixtureTask;
import com.example.labdata.payload.response.MixratioSpecimenPairResponse;
import com.example.labdata.payload.response.MixRatioDetailResponse;
import com.example.labdata.repository.MixtureTaskRepository;
import com.example.labdata.repository.MixRatioRepository;
import com.example.labdata.repository.ProjectRepository;
import com.example.labdata.repository.UserMixtureTaskRepository;
import com.example.labdata.repository.MixRatioAsphaltRepository;
import com.example.labdata.repository.MixRatioSandRepository;
import com.example.labdata.repository.MixRatioStoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MixtureTaskService {

    private final UserMixtureTaskRepository userMixtureTaskRepository;
    private final MixtureTaskRepository mixtureTaskRepository;
    private final ProjectRepository projectRepository;
    private final MixRatioRepository mixRatioRepository;
    private final MixRatioAsphaltRepository mixRatioAsphaltRepository;
    private final MixRatioSandRepository mixRatioSandRepository;
    private final MixRatioStoneRepository mixRatioStoneRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskService.class);

    @Autowired
    public MixtureTaskService(UserMixtureTaskRepository userMixtureTaskRepository, 
                             MixtureTaskRepository mixtureTaskRepository,
                             ProjectRepository projectRepository,
                             MixRatioRepository mixRatioRepository,
                             MixRatioAsphaltRepository mixRatioAsphaltRepository,
                             MixRatioSandRepository mixRatioSandRepository,
                             MixRatioStoneRepository mixRatioStoneRepository,
                             JdbcTemplate jdbcTemplate) {
        this.userMixtureTaskRepository = userMixtureTaskRepository;
        this.mixtureTaskRepository = mixtureTaskRepository;
        this.projectRepository = projectRepository;
        this.mixRatioRepository = mixRatioRepository;
        this.mixRatioAsphaltRepository = mixRatioAsphaltRepository;
        this.mixRatioSandRepository = mixRatioSandRepository;
        this.mixRatioStoneRepository = mixRatioStoneRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

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
     * @param taskId 任务ID（数据库中的主键ID）
     * @return 项目名称，如果没有找到则返回"未知项目"
     */
    public String getProjectNameByTaskId(Long taskId) {
        logger.info("获取任务ID: {} 的项目名称", taskId);
        
        // 首先尝试使用主键ID查找任务记录
        Optional<UserMixtureTask> taskByIdOpt = userMixtureTaskRepository.findById(taskId);
        if (taskByIdOpt.isPresent()) {
            UserMixtureTask task = taskByIdOpt.get();
            logger.info("通过主键ID={}找到任务记录: {}, task_id={}, 项目ID={}", 
                         taskId, task.getTaskName(), task.getTaskId(), task.getProjectId());
            
            if (task.getProjectId() != null) {
                // 使用project_id到projects表中查询项目
                logger.info("正在查询项目ID: {}", task.getProjectId());
                Optional<Project> projectOpt = projectRepository.findByProjectId(task.getProjectId());
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    logger.info("找到项目: {}", project.getName());
                    return project.getName();
                } else {
                    logger.info("在projects表中未找到project_id={}的项目", task.getProjectId());
                }
            } else {
                logger.info("任务的项目ID为null");
            }
        } else {
            logger.info("在mixture_task表中未找到主键ID={}的任务，尝试使用task_id查询", taskId);
            
            // 如果未找到，尝试将数字ID转换为字符串作为task_id查询
            // 这是为了兼容可能的不同调用方式
            List<UserMixtureTask> userTasks = userMixtureTaskRepository.findByTaskId(String.valueOf(taskId));
            if (!userTasks.isEmpty()) {
                UserMixtureTask userTask = userTasks.get(0);
                logger.info("在mixture_task表中找到任务(task_id={}): {}，项目ID: {}", 
                            taskId, userTask.getTaskName(), userTask.getProjectId());
                
                if (userTask.getProjectId() != null) {
                    // 使用project_id到projects表中查询项目
                    logger.info("正在查询项目ID: {}", userTask.getProjectId());
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
                logger.info("在mixture_task表中未找到task_id={}的任务", taskId);
            }
        }
        
        return "未知项目";
    }
    
    /**
     * 根据任务ID获取mixratio_id和specimen_id组合列表
     * 
     * @param taskId 任务ID
     * @return 配比和制件ID组合列表（已去重）
     */
    public List<MixratioSpecimenPairResponse> getMixratioSpecimenPairsByTaskId(Long taskId) {
        logger.info("获取任务ID: {} 的配比和制件组合", taskId);
        // 使用Set来存储不重复的组合
        Set<MixratioSpecimenPairResponse> uniquePairs = new HashSet<>();
        List<UserMixtureTask> userTasks = new ArrayList<>();
        
        // 1. 首先检查是否为主键ID查询
        Optional<UserMixtureTask> taskByPrimaryId = userMixtureTaskRepository.findById(taskId);
        if (taskByPrimaryId.isPresent()) {
            UserMixtureTask primaryTask = taskByPrimaryId.get();
            logger.info("通过主键ID={}找到任务记录: {}, task_id={}", 
                         taskId, primaryTask.getTaskName(), primaryTask.getTaskId());
            
            // 使用找到记录的task_id查询所有相关记录
            if (primaryTask.getTaskId() != null && !primaryTask.getTaskId().isEmpty()) {
                // 提取task_id的基础部分（移除末尾的索引）
                String taskIdBase = primaryTask.getTaskId();
                int lastDashIndex = taskIdBase.lastIndexOf('-');
                if (lastDashIndex > 0) {
                    taskIdBase = taskIdBase.substring(0, lastDashIndex + 1);
                    logger.info("提取task_id基础前缀: {}", taskIdBase);
                    
                    // 使用前缀进行模糊查询
                    userTasks = userMixtureTaskRepository.findByTaskIdStartingWith(taskIdBase);
                    logger.info("通过task_id前缀={}找到{}条相关记录", taskIdBase, userTasks.size());
                } else {
                    // 如果task_id没有破折号分隔符，使用完整task_id查询
                    logger.info("task_id没有分隔符，使用完整task_id={}查询", taskIdBase);
                    userTasks = userMixtureTaskRepository.findByTaskId(taskIdBase);
                    logger.info("通过完整task_id={}找到{}条相关记录", taskIdBase, userTasks.size());
                }
            }
            
            // 如果没有找到记录或task_id为空，至少返回当前记录
            if (userTasks.isEmpty()) {
                logger.info("未找到相关记录或task_id为空，使用当前记录");
                userTasks.add(primaryTask);
            }
        } else {
            // 2. 直接使用数字ID作为task_id查询
            String taskIdStr = String.valueOf(taskId);
            logger.info("未找到主键ID={}的记录，尝试使用{}作为task_id查询", taskId, taskIdStr);
            userTasks = userMixtureTaskRepository.findByTaskId(taskIdStr);
            logger.info("通过task_id={}找到{}条记录", taskIdStr, userTasks.size());
        }
        
        // 处理找到的所有任务记录，添加到Set中自动去重
        for (UserMixtureTask userTask : userTasks) {
            logger.info("处理任务记录: ID={}, 任务名称={}, 配比ID={}, 制件ID={}", 
                        userTask.getId(), userTask.getTaskName(), userTask.getMixratioId(), userTask.getSpecimenId());
            
            if (userTask.getMixratioId() != null && userTask.getSpecimenId() != null) {
                // 查询配比名称
                String mixName = "未知配比";
                try {
                    Optional<MixRatio> mixRatio = mixRatioRepository.findById(userTask.getMixratioId());
                    if (mixRatio.isPresent()) {
                        mixName = mixRatio.get().getMixName();
                        logger.debug("找到配比名称: {}", mixName);
                    } else {
                        logger.warn("未找到ID={}的配比信息", userTask.getMixratioId());
                    }
                } catch (Exception e) {
                    logger.error("查询配比名称时出错: {}", e.getMessage());
                }
                
                MixratioSpecimenPairResponse pair = new MixratioSpecimenPairResponse(
                        userTask.getMixratioId(), userTask.getSpecimenId(), mixName);
                uniquePairs.add(pair);
                logger.debug("添加配比-制件组合: [{}, {}, {}], 当前去重后组合数: {}", 
                        userTask.getMixratioId(), userTask.getSpecimenId(), mixName, uniquePairs.size());
            }
        }
        
        // 转换Set为List返回
        List<MixratioSpecimenPairResponse> result = new ArrayList<>(uniquePairs);
        logger.info("任务ID: {} 共找到 {} 个不重复配比和制件组合", taskId, result.size());
        return result;
    }
    
    /**
     * 根据任务ID获取配比详细信息列表
     * 
     * @param taskId 任务ID
     * @return 配比详细信息列表
     */
    public List<MixRatioDetailResponse> getMixRatioDetailsByTaskId(Long taskId) {
        logger.info("获取任务配比详情，任务ID: {}", taskId);
        List<MixRatioDetailResponse> results = new ArrayList<>();
        List<Long> mixRatioIds = new ArrayList<>();
        
        // 1. 首先检查是否为主键ID查询
        Optional<UserMixtureTask> taskByPrimaryId = userMixtureTaskRepository.findById(taskId);
        if (taskByPrimaryId.isPresent()) {
            UserMixtureTask primaryTask = taskByPrimaryId.get();
            logger.info("通过主键ID={}找到任务记录: {}, task_id={}", 
                         taskId, primaryTask.getTaskName(), primaryTask.getTaskId());
            
            // 使用找到记录的task_id查询所有相关记录对应的mixratio_id（去重）
            if (primaryTask.getTaskId() != null && !primaryTask.getTaskId().isEmpty()) {
                // 提取task_id的基础部分（移除末尾的索引）
                String taskIdBase = primaryTask.getTaskId();
                int lastDashIndex = taskIdBase.lastIndexOf('-');
                if (lastDashIndex > 0) {
                    taskIdBase = taskIdBase.substring(0, lastDashIndex + 1);
                    logger.info("提取task_id基础前缀: {}", taskIdBase);
                    
                    // 使用前缀进行模糊查询
                    mixRatioIds = mixRatioRepository.findDistinctMixratioIdsByTaskIdStartingWith(taskIdBase);
                    logger.info("通过task_id前缀={}找到{}个不同配比", taskIdBase, mixRatioIds.size());
                } else {
                    // 如果task_id没有破折号分隔符，使用完整task_id查询
                    logger.info("task_id没有分隔符，使用完整task_id={}查询", taskIdBase);
                    mixRatioIds = mixRatioRepository.findDistinctMixratioIdsByTaskId(taskIdBase);
                    logger.info("通过完整task_id={}找到{}个不同配比", taskIdBase, mixRatioIds.size());
                }
            }
            
            // 如果没有找到配比ID，至少添加当前任务的配比ID
            if (mixRatioIds.isEmpty() && primaryTask.getMixratioId() != null) {
                mixRatioIds.add(primaryTask.getMixratioId());
                logger.info("未通过task_id查询到配比ID，使用当前任务的配比ID: {}", primaryTask.getMixratioId());
            }
        } else {
            // 直接使用数字ID作为task_id查询
            String taskIdStr = String.valueOf(taskId);
            logger.info("未找到主键ID={}的记录，尝试使用{}作为task_id查询", taskId, taskIdStr);
            mixRatioIds = mixRatioRepository.findDistinctMixratioIdsByTaskId(taskIdStr);
            logger.info("通过task_id={}找到{}个不同配比", taskIdStr, mixRatioIds.size());
        }
        
        // 2. 根据配比ID获取详细信息
        for (Long mixRatioId : mixRatioIds) {
            MixRatioDetailResponse detail = getMixRatioDetailById(mixRatioId);
            if (detail != null) {
                results.add(detail);
                logger.debug("添加配比详情: {}", detail);
            }
        }
        
        logger.info("任务ID: {} 共找到 {} 个配比详情", taskId, results.size());
        return results;
    }
    
    /**
     * 根据配比ID获取配比详细信息
     * 
     * @param mixRatioId 配比ID
     * @return 配比详细信息
     */
    private MixRatioDetailResponse getMixRatioDetailById(Long mixRatioId) {
        logger.debug("获取配比详情，配比ID: {}", mixRatioId);
        
        // 1. 获取配比基本信息
        Optional<MixRatio> mixRatioOpt = mixRatioRepository.findById(mixRatioId);
        if (mixRatioOpt.isEmpty()) {
            logger.warn("未找到ID={}的配比信息", mixRatioId);
            return null;
        }
        
        MixRatio mixRatio = mixRatioOpt.get();
        MixRatioDetailResponse response = new MixRatioDetailResponse();
        response.setMixratioId(mixRatioId);
        response.setMixName(mixRatio.getMixName());
        
        try {
            // 2. 获取沥青信息
            List<MixRatioAsphalt> asphaltList = mixRatioAsphaltRepository.findByMixRatioId(mixRatioId);
            if (!asphaltList.isEmpty()) {
                MixRatioAsphalt asphalt = asphaltList.get(0); // 通常只有一种沥青
                response.setAsphaltName(asphalt.getAsphaltMaterial().getName());
                response.setAsphaltPercentage(asphalt.getPercentage());
                logger.debug("找到配比{}的沥青: {}, 百分比: {}", mixRatioId, 
                             asphalt.getAsphaltMaterial().getName(), asphalt.getPercentage());
            }
            
            // 3. 获取沙子信息
            List<MixRatioSand> sandList = mixRatioSandRepository.findByMixRatioId(mixRatioId);
            if (!sandList.isEmpty()) {
                MixRatioSand sand = sandList.get(0); // 通常只使用一种沙子
                response.setSandName(sand.getSandMaterial().getName());
                response.setSandPercentage(sand.getPercentage());
                response.setSandGradation(sand.getGradation());
                logger.debug("找到配比{}的沙子: {}, 百分比: {}, 级配: {}", mixRatioId, 
                             sand.getSandMaterial().getName(), sand.getPercentage(), sand.getGradation());
            }
            
            // 4. 获取石子信息
            List<MixRatioStone> stoneList = mixRatioStoneRepository.findByMixRatioId(mixRatioId);
            if (!stoneList.isEmpty()) {
                MixRatioStone stone = stoneList.get(0); // 可能有多种石子，这里简化处理取第一个
                response.setStoneName(stone.getStoneMaterial().getName());
                response.setStonePercentage(stone.getPercentage());
                response.setStoneGradation(stone.getGradation());
                logger.debug("找到配比{}的石子: {}, 百分比: {}, 级配: {}", mixRatioId, 
                             stone.getStoneMaterial().getName(), stone.getPercentage(), stone.getGradation());
            }
            
            return response;
        } catch (Exception e) {
            logger.error("获取配比详情时出错: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 为任务列表设置项目名称
     * 
     * @param tasks 任务列表
     */
    private void setProjectNamesForTasks(List<MixtureTask> tasks) {
        for (MixtureTask task : tasks) {
            if (task.getProjectId() != null) {
                try {
                    // 将projectId从String转换为Long
                    Long projectIdLong = Long.parseLong(task.getProjectId());
                    Optional<Project> projectOpt = projectRepository.findById(projectIdLong);
                    if (projectOpt.isPresent()) {
                        Project project = projectOpt.get();
                        task.setProject(project);
                        task.setProjectName(project.getName());
                    } else {
                        // 如果找不到项目，设置一个默认名称
                        task.setProjectName("未知项目");
                    }
                } catch (NumberFormatException e) {
                    logger.warn("无法解析项目ID: {}", task.getProjectId());
                    task.setProjectName("无效项目ID");
                }
            } else {
                task.setProjectName("无项目");
            }
        }
    }
    
    /**
     * 获取任务的实验指派信息
     * 步骤：
     * 1. 使用id查询获取对应的任务
     * 2. 使用task_id前缀查询获取所有相关任务
     * 3. 获取所有任务的mixratio_id并去重
     * 4. 对每个mixratio_id，收集并去重其对应的task_assignment
     * 5. 返回mixratio_id到task_assignment的映射
     * 
     * @param taskId 任务ID（数据库主键id）
     * @return 配比ID到实验指派的映射
     */
    public Map<Long, List<String>> getTaskAssignmentsByTaskId(Long taskId) {
        logger.info("获取任务ID: {} 的实验指派信息", taskId);
        
        try {
            // 1. 使用id查询获取对应的任务
            Optional<UserMixtureTask> taskOptional = userMixtureTaskRepository.findById(taskId);
            
            if (taskOptional.isEmpty()) {
                logger.warn("未找到ID: {} 的任务记录", taskId);
                return new HashMap<>();
            }
            
            // 获取task_id字符串
            String taskIdStr = taskOptional.get().getTaskId();
            logger.info("找到任务ID: {}, 对应的task_id字符串: {}", taskId, taskIdStr);
            
            // 提取task_id的前缀部分（第一个连字符之前的部分）
            String taskIdPrefix = taskIdStr;
            int dashIndex = taskIdStr.indexOf('-');
            if (dashIndex > 0) {
                taskIdPrefix = taskIdStr.substring(0, dashIndex + 1); // 包含连字符
                logger.info("提取task_id前缀: {}", taskIdPrefix);
            }
            
            // 2. 使用task_id字符串前缀查询所有相关任务
            List<UserMixtureTask> userMixtureTasks = userMixtureTaskRepository.findByTaskIdStartingWith(taskIdPrefix);
            
            if (userMixtureTasks.isEmpty()) {
                logger.warn("未找到task_id前缀: {} 的相关任务", taskIdPrefix);
                return new HashMap<>();
            }
            
            logger.info("找到 {} 条相关任务记录", userMixtureTasks.size());
            
            // 3. 提取所有唯一的mixratio_id
            Set<Long> uniqueMixratioIds = new HashSet<>();
            for (UserMixtureTask task : userMixtureTasks) {
                uniqueMixratioIds.add(task.getMixratioId());
            }
            
            logger.info("找到 {} 个唯一的配比ID", uniqueMixratioIds.size());
            
            // 4. 对每个mixratio_id，收集并去重其对应的task_assignment
            Map<Long, List<String>> assignmentsMap = new HashMap<>();
            
            for (Long mixratioId : uniqueMixratioIds) {
                // 找到此配比ID对应的所有任务
                List<String> assignments = new ArrayList<>();
                
                for (UserMixtureTask task : userMixtureTasks) {
                    if (task.getMixratioId().equals(mixratioId)) {
                        String taskAssignment = task.getTaskAssignment();
                        if (taskAssignment != null && !taskAssignment.trim().isEmpty() 
                                && !assignments.contains(taskAssignment)) {
                            assignments.add(taskAssignment);
                        }
                    }
                }
                
                if (!assignments.isEmpty()) {
                    assignmentsMap.put(mixratioId, assignments);
                    logger.info("配比ID: {} 有 {} 个唯一实验指派", mixratioId, assignments.size());
                }
            }
            
            logger.info("任务ID: {} 的实验指派信息获取成功，共 {} 个配比有实验指派", 
                       taskId, assignmentsMap.size());
            return assignmentsMap;
        } catch (Exception e) {
            logger.error("获取任务实验指派信息时出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取任务的备注信息
     * 
     * @param taskId 任务ID（数据库主键id）
     * @return 备注信息，如果没有备注则返回"无备注"
     */
    public String getTaskRemarksByTaskId(Long taskId) {
        logger.info("获取任务ID: {} 的备注信息", taskId);
        
        try {
            // 1. 使用id查询获取对应的任务
            Optional<UserMixtureTask> taskOptional = userMixtureTaskRepository.findById(taskId);
            
            if (taskOptional.isEmpty()) {
                logger.warn("未找到ID: {} 的任务记录", taskId);
                return "无备注";
            }
            
            // 获取remarks字段
            String remarks = taskOptional.get().getRemarks();
            
            // 如果备注为空，则返回"无备注"
            if (remarks == null || remarks.trim().isEmpty()) {
                logger.info("任务ID: {} 没有备注信息", taskId);
                return "无备注";
            }
            
            logger.info("任务ID: {} 的备注信息获取成功: {}", taskId, remarks);
            return remarks;
        } catch (Exception e) {
            logger.error("获取任务备注信息时出错: {}", e.getMessage(), e);
            return "无备注";
        }
    }

    /**
     * 保存设备信息
     * 
     * @param taskId 任务ID
     * @param deviceType 设备类型
     * @param deviceModel 设备型号
     * @param manufacturer 设备厂家，可能为null
     * @return 设备信息和类型
     */
    public Map<String, String> saveDeviceInfo(String taskId, String deviceType, String deviceModel, String manufacturer) {
        logger.info("保存设备信息，任务ID: {}, 设备类型: {}, 设备型号: {}, 厂家: {}", 
                     taskId, deviceType, deviceModel, manufacturer);
        
        // 提取任务ID前缀（去掉"-0"、"-1"等后缀）
        String taskIdPrefix = extractTaskIdPrefix(taskId);
        logger.info("提取的任务ID前缀: {}", taskIdPrefix);
        
        // 使用JDBC Template直接执行更新，避免JPA的懒加载问题
        try {
            // 查询所有匹配前缀的任务ID
            String searchPattern = taskIdPrefix + "%";
            String findSql = "SELECT task_id FROM mixture_task WHERE task_id LIKE ?";
            
            List<String> taskIds = jdbcTemplate.query(findSql, (rs, rowNum) -> rs.getString("task_id"), searchPattern);
            
            if (taskIds.isEmpty()) {
                logger.warn("未找到任务ID前缀: {}", taskIdPrefix);
                throw new RuntimeException("未找到任务: " + taskId);
            }
            
            logger.info("找到{}个匹配前缀{}的任务记录", taskIds.size(), taskIdPrefix);
            
            // 准备更新语句
            String equipmentColumn;
            String manufacturerColumn;
            switch (deviceType.toUpperCase()) {
                case "MIXING":
                    equipmentColumn = "assigned_mixing_equipment";
                    manufacturerColumn = "mixing_equipment_manufacturer";
                    break;
                case "FORMING":
                    equipmentColumn = "assigned_forming_equipment";
                    manufacturerColumn = "forming_equipment_manufacturer";
                    break;
                case "TESTING":
                    equipmentColumn = "assigned_testing_equipment";
                    manufacturerColumn = "testing_equipment_manufacturer";
                    break;
                default:
                    logger.warn("不支持的设备类型: {}", deviceType);
                    throw new RuntimeException("不支持的设备类型: " + deviceType);
            }
            
            // 对所有匹配的任务ID执行更新
            int totalUpdatedRows = 0;
            for (String foundTaskId : taskIds) {
                String updateSql;
                int updatedRows;
                
                if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                    // 同时更新设备型号和厂家
                    updateSql = "UPDATE mixture_task SET " + equipmentColumn + " = ?, " + manufacturerColumn + " = ? WHERE task_id = ?";
                    updatedRows = jdbcTemplate.update(updateSql, deviceModel, manufacturer, foundTaskId);
                } else {
                    // 只更新设备型号
                    updateSql = "UPDATE mixture_task SET " + equipmentColumn + " = ? WHERE task_id = ?";
                    updatedRows = jdbcTemplate.update(updateSql, deviceModel, foundTaskId);
                }
                
                totalUpdatedRows += updatedRows;
                logger.info("更新任务 {}: {} 行受影响", foundTaskId, updatedRows);
            }
            
            if (totalUpdatedRows == 0) {
                logger.warn("未成功更新任何任务");
                throw new RuntimeException("未能更新设备信息");
            }
            
            logger.info("成功更新了{}个任务的设备信息", totalUpdatedRows);
            
            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("type", deviceType.toUpperCase());
            result.put("model", deviceModel);
            if (manufacturer != null && !manufacturer.trim().isEmpty()) {
                result.put("manufacturer", manufacturer);
            }
            return result;
            
        } catch (Exception e) {
            if (!(e instanceof RuntimeException)) {
                logger.error("保存设备信息时发生错误", e);
                throw new RuntimeException("保存设备信息失败: " + e.getMessage(), e);
            }
            throw e;
        }
    }
    
    /**
     * 为了保持向后兼容，增加一个不带厂家参数的重载方法
     */
    public Map<String, String> saveDeviceInfo(String taskId, String deviceType, String deviceModel) {
        return saveDeviceInfo(taskId, deviceType, deviceModel, null);
    }
    
    /**
     * 提取任务ID前缀（去掉-后面的数字后缀）
     * 例如: "58c3d798-89bc-4e91-81b0-7dee2ae4fae5-0" -> "58c3d798-89bc-4e91-81b0-7dee2ae4fae5"
     */
    private String extractTaskIdPrefix(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return taskId;
        }
        
        int lastDashIndex = taskId.lastIndexOf("-");
        if (lastDashIndex > 0) {
            // 检查破折号后面是否都是数字
            String suffix = taskId.substring(lastDashIndex + 1);
            if (suffix.matches("\\d+")) {
                return taskId.substring(0, lastDashIndex);
            }
        }
        
        return taskId;
    }

    /**
     * 获取试件制备所需的方法、配比和设备信息
     * 
     * @param taskIdPrefix 任务ID前缀
     * @return 包含制件方法、设备信息的Map
     */
    public Map<String, Object> getSpecimenData(String taskId) {
        // 从taskId中提取前缀部分（去掉最后的-数字后缀）
        String taskIdPrefix = taskId;
        if (taskId.matches(".*-\\d+$")) {
            taskIdPrefix = taskId.substring(0, taskId.lastIndexOf('-'));
        }
        
        logger.info("获取任务ID前缀: {} 的试件制备数据", taskIdPrefix);
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 使用原生SQL查询获取相关任务，避免Hibernate的类型转换问题
            List<Map<String, Object>> mixtureTasks = mixtureTaskRepository.findAllByTaskIdPrefixNative(taskIdPrefix);
            if (mixtureTasks.isEmpty()) {
                logger.warn("未找到匹配前缀: {} 的任务", taskIdPrefix);
                return result;
            }
            
            // 2. 直接使用原生SQL查询与任务前缀相关的specimen_id
            List<Long> specimenIds = new ArrayList<>();
            try {
                String specimenIdQuery = "SELECT DISTINCT CAST(specimen_id AS BIGINT) FROM mixture_task " +
                                       "WHERE task_id LIKE ? AND specimen_id IS NOT NULL";
                specimenIds = jdbcTemplate.queryForList(specimenIdQuery, Long.class, taskIdPrefix + "%");
                logger.info("为任务前缀 {} 找到 {} 个试件ID", taskIdPrefix, specimenIds.size());
            } catch (Exception e) {
                logger.error("查询specimen_id时出错: {}", e.getMessage(), e);
                // 尝试使用文本格式查询
                try {
                    String altQuery = "SELECT DISTINCT specimen_id::text FROM mixture_task " +
                                     "WHERE task_id LIKE ? AND specimen_id IS NOT NULL";
                    List<String> idStrings = jdbcTemplate.queryForList(altQuery, String.class, taskIdPrefix + "%");
                    for (String idStr : idStrings) {
                        try {
                            specimenIds.add(Long.parseLong(idStr));
                        } catch (NumberFormatException nfe) {
                            logger.warn("无法解析试件ID: {}", idStr);
                        }
                    }
                    logger.info("使用备选查询方式为任务前缀 {} 找到 {} 个试件ID", taskIdPrefix, specimenIds.size());
                } catch (Exception ex) {
                    logger.error("备选查询方式也失败: {}", ex.getMessage(), ex);
                }
            }
            
            if (specimenIds.isEmpty()) {
                logger.warn("任务前缀: {} 对应的任务没有关联的试件", taskIdPrefix);
                return result;
            }
            
            // 3. 根据specimenIds从specimens表获取制件方法信息
            List<Map<String, Object>> methodsAndRatios = new ArrayList<>();
            for (Long specimenId : specimenIds) {
                try {
                    // 从specimens表获取试件信息，确保compaction_method不为空
                    String sql = "SELECT id, " +
                               "mixing_temperature, " + 
                               "mixing_speed, " + 
                               "mixing_time, " + 
                               "COALESCE(compaction_method, '标准压实') as compaction_method " + 
                               "FROM specimens WHERE id = ?";
                    List<Map<String, Object>> specimens = jdbcTemplate.queryForList(sql, specimenId);
                    
                    if (specimens.isEmpty()) {
                        // 如果未找到specimen记录，记录警告
                        logger.warn("未找到specimen ID {} 的记录", specimenId);
                    } else {
                        // 记录找到的compaction_method
                        for (Map<String, Object> specimen : specimens) {
                            logger.info("获取到specimen ID {} 的压实方法: {}", 
                                specimenId, specimen.get("compaction_method"));
                        }
                        methodsAndRatios.addAll(specimens);
                    }
                } catch (Exception e) {
                    logger.error("查询specimen ID {} 的信息时出错: {}", specimenId, e.getMessage());
                }
            }
            
            // 从mixture_task表直接获取project_id
            String projectId = "";
            try {
                String projectIdQuery = "SELECT project_id FROM mixture_task WHERE task_id LIKE ? AND project_id IS NOT NULL LIMIT 1";
                projectId = jdbcTemplate.queryForObject(projectIdQuery, String.class, taskIdPrefix + "%");
                logger.info("从mixture_task表获取project_id: {}", projectId);
            } catch (Exception ex) {
                logger.error("从mixture_task表获取project_id失败: {}", ex.getMessage());
                projectId = "";
            }
            
            // 应用project_id到所有方法数据中
            for (Map<String, Object> method : methodsAndRatios) {
                method.put("project_id", projectId);
            }
            
            // 获取配比信息
            try {
                // 从mixture_task表获取mixratio_id
                String mixratioIdQuery = "SELECT DISTINCT mixratio_id FROM mixture_task WHERE task_id LIKE ? AND mixratio_id IS NOT NULL";
                List<Long> mixratioIds = jdbcTemplate.queryForList(mixratioIdQuery, Long.class, taskIdPrefix + "%");
                logger.info("为任务前缀 {} 找到 {} 个配比ID", taskIdPrefix, mixratioIds.size());
                
                // 根据mixratio_id从mixratio表获取配比名称
                if (!mixratioIds.isEmpty()) {
                    StringBuilder inClause = new StringBuilder();
                    for (int i = 0; i < mixratioIds.size(); i++) {
                        inClause.append("?");
                        if (i < mixratioIds.size() - 1) {
                            inClause.append(",");
                        }
                    }
                    
                    // 注意：这里我们不再从mixratio表获取project_id，因为我们已经从mixture_task表直接获取了
                    String mixratioQuery = "SELECT id, mix_name FROM mixratio WHERE id IN (" + inClause.toString() + ")";
                    
                    // 转换参数为Object数组
                    Object[] params = mixratioIds.toArray();
                    
                    List<Map<String, Object>> mixratios = jdbcTemplate.queryForList(mixratioQuery, params);
                    logger.info("查询到 {} 个配比信息", mixratios.size());
                    
                    // 将配比信息添加到方法数据中
                    for (Map<String, Object> method : methodsAndRatios) {
                        if (!mixratios.isEmpty()) {
                            method.put("mix_name", mixratios.get(0).get("mix_name"));
                        } else {
                            method.put("mix_name", "标准配比");
                        }
                    }
                } else {
                    // 设置默认配比名称
                    for (Map<String, Object> method : methodsAndRatios) {
                        method.put("mix_name", "标准配比");
                    }
                }
            } catch (Exception e) {
                logger.error("查询配比信息时出错: {}", e.getMessage(), e);
                // 设置默认配比名称
                for (Map<String, Object> method : methodsAndRatios) {
                    method.put("mix_name", "标准配比");
                }
            }
            
            result.put("methodsAndRatios", methodsAndRatios);
            
            // 4. 获取混合设备信息
            try {
                List<Map<String, Object>> mixingEquipment = jdbcTemplate.queryForList(
                    "SELECT DISTINCT assigned_mixing_equipment as deviceId, " +
                    "COALESCE(mixing_equipment_manufacturer, '标准制造商') as manufacturer, " +
                    "'mixing' as deviceType, " + 
                    "assigned_mixing_equipment as model " +
                    "FROM mixture_task WHERE task_id LIKE ? OR task_id = ? AND assigned_mixing_equipment IS NOT NULL",
                    taskIdPrefix + "%", taskIdPrefix
                );
                
                result.put("mixingEquipment", mixingEquipment);
            } catch (Exception e) {
                logger.error("查询混合设备信息时出错: {}", e.getMessage());
                result.put("mixingEquipment", new ArrayList<>());
            }
            
            // 5. 获取成型设备信息
            try {
                List<Map<String, Object>> formingEquipment = jdbcTemplate.queryForList(
                    "SELECT DISTINCT assigned_forming_equipment as deviceId, " +
                    "COALESCE(forming_equipment_manufacturer, '标准制造商') as manufacturer, " +
                    "'forming' as deviceType, " +
                    "assigned_forming_equipment as model " +
                    "FROM mixture_task WHERE task_id LIKE ? OR task_id = ? AND assigned_forming_equipment IS NOT NULL",
                    taskIdPrefix + "%", taskIdPrefix
                );
                
                result.put("formingEquipment", formingEquipment);
            } catch (Exception e) {
                logger.error("查询成型设备信息时出错: {}", e.getMessage());
                result.put("formingEquipment", new ArrayList<>());
            }
            
            logger.info("成功获取任务前缀: {} 的试件制备数据", taskIdPrefix);
            return result;
        } catch (Exception e) {
            logger.error("获取试件制备数据时出错: {}", e.getMessage(), e);
            return result;
        }
    }

    /**
     * 更新指定前缀任务ID的制件状态为"已完成"
     * 
     * @param taskIdPrefix 任务ID前缀
     * @return 更新成功返回true，否则返回false
     */
    public boolean updateMakingStatusToFinished(String taskIdPrefix) {
        try {
            logger.info("准备更新任务ID前缀为{}的所有任务制件状态为'已完成'", taskIdPrefix);
            
            // 方法1: 使用自定义查询方法更新
            int updatedCount = mixtureTaskRepository.updateMakingStatusByTaskIdPrefix(taskIdPrefix, "finished");
            
            // 或者使用 JDBC 原生 SQL 更新
            if (updatedCount == 0) {
                logger.info("使用原生SQL更新任务制件状态");
                String sql = "UPDATE mixture_task SET making_status = 'finished' WHERE task_id LIKE ? OR task_id = ?";
                updatedCount = jdbcTemplate.update(sql, taskIdPrefix + "%", taskIdPrefix);
                logger.info("使用原生SQL更新结果: {} 条记录", updatedCount);
            }
            
            logger.info("更新了{}条记录", updatedCount);
            return updatedCount > 0;
        } catch (Exception e) {
            logger.error("更新任务制件状态时出错: {}", e.getMessage(), e);
            return false;
        }
    }
}
