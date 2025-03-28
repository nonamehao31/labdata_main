package com.example.labdata.service;

import com.example.labdata.model.MixtureTask;
import com.example.labdata.model.MixRatio;
import com.example.labdata.model.MixRatioAsphalt;
import com.example.labdata.model.MixRatioSand;
import com.example.labdata.model.MixRatioStone;
import com.example.labdata.model.Project;
import com.example.labdata.model.SupportMixtureTask;
import com.example.labdata.model.UserMixtureTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.labdata.payload.response.MixratioSpecimenPairResponse;
import com.example.labdata.payload.response.MixRatioDetailResponse;
import com.example.labdata.payload.response.MixratioAndCompactionResponse;
import com.example.labdata.payload.response.ApiResponse;
import com.example.labdata.repository.MixtureTaskRepository;
import com.example.labdata.repository.MixRatioRepository;
import com.example.labdata.repository.ProjectRepository;
import com.example.labdata.repository.UserMixtureTaskRepository;
import com.example.labdata.repository.MixRatioAsphaltRepository;
import com.example.labdata.repository.MixRatioSandRepository;
import com.example.labdata.repository.MixRatioStoneRepository;
import com.example.labdata.repository.SupportMixtureTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.sql.Timestamp;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MixtureTaskService {

    private final UserMixtureTaskRepository userMixtureTaskRepository;
    private final MixtureTaskRepository mixtureTaskRepository;
    private final ProjectRepository projectRepository;
    private final MixRatioRepository mixRatioRepository;
    private final MixRatioAsphaltRepository mixRatioAsphaltRepository;
    private final MixRatioSandRepository mixRatioSandRepository;
    private final MixRatioStoneRepository mixRatioStoneRepository;
    private final SupportMixtureTaskRepository supportMixtureTaskRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(MixtureTaskService.class);

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskService.class);

    @Autowired
    public MixtureTaskService(UserMixtureTaskRepository userMixtureTaskRepository,
                              MixtureTaskRepository mixtureTaskRepository,
                              ProjectRepository projectRepository,
                              MixRatioRepository mixRatioRepository,
                              MixRatioAsphaltRepository mixRatioAsphaltRepository,
                              MixRatioSandRepository mixRatioSandRepository,
                              MixRatioStoneRepository mixRatioStoneRepository,
                              SupportMixtureTaskRepository supportMixtureTaskRepository,
                              JdbcTemplate jdbcTemplate) {
        this.userMixtureTaskRepository = userMixtureTaskRepository;
        this.mixtureTaskRepository = mixtureTaskRepository;
        this.projectRepository = projectRepository;
        this.mixRatioRepository = mixRatioRepository;
        this.mixRatioAsphaltRepository = mixRatioAsphaltRepository;
        this.mixRatioSandRepository = mixRatioSandRepository;
        this.mixRatioStoneRepository = mixRatioStoneRepository;
        this.supportMixtureTaskRepository = supportMixtureTaskRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<MixtureTask> getAllMixtureTasks() {
        List<MixtureTask> tasks = mixtureTaskRepository.findAll();
        // 查询并设置每个任务的项目名称
        setProjectNamesForTasks(tasks);
        return tasks;
    }

    // 记录当前的 getMixtureTasksByType 方法，稍后修改
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
                    taskIdBase = taskIdBase.substring(0, lastDashIndex + 1); // 包含连字符
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

            logger.info("任务ID: {} 的实验指派信息获取成功，共 {} 个配比ID",
                    taskId, assignmentsMap.size());
            return assignmentsMap;
        } catch (Exception e) {
            logger.error("获取任务实验指派信息时出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 根据任务ID获取mixratio_id和specimen_id组合列表
     *
     * @param taskIdPrefix 任务ID前缀
     * @return 配比和制件ID组合列表（已去重）
     */
    public List<MixratioSpecimenPairResponse> getMixratioSpecimenPairsByTaskIdPrefix(String taskIdPrefix) {
        logger.info("获取任务ID前缀: {} 的配比和制件组合", taskIdPrefix);
        // 使用Set来存储不重复的组合
        Set<MixratioSpecimenPairResponse> uniquePairs = new HashSet<>();
        List<UserMixtureTask> userTasks = new ArrayList<>();

        // 1. 使用task_id前缀查询所有相关任务
        userTasks = userMixtureTaskRepository.findByTaskIdStartingWith(taskIdPrefix);

        if (userTasks.isEmpty()) {
            logger.warn("未找到task_id前缀: {} 的相关任务", taskIdPrefix);
            return new ArrayList<>();
        }

        logger.info("找到 {} 条相关任务记录", userTasks.size());

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
        logger.info("任务ID前缀: {} 共找到 {} 个不重复配比和制件组合", taskIdPrefix, result.size());
        return result;
    }

    /**
     * 根据任务ID前缀获取实验指派信息
     *
     * @param taskIdPrefix 任务ID前缀
     * @return 实验指派信息
     */
    public ApiResponse<List<Map<String, Object>>> getTaskAssignmentsByTaskIdPrefix(String taskIdPrefix) {
        logger.info("获取任务ID前缀: {} 的实验指派信息", taskIdPrefix);

        try {
            // 1. 使用task_id前缀查询所有相关任务
            List<UserMixtureTask> userMixtureTasks = userMixtureTaskRepository.findByTaskIdStartingWith(taskIdPrefix);

            if (userMixtureTasks.isEmpty()) {
                logger.warn("未找到task_id前缀: {} 的相关任务", taskIdPrefix);
                return new ApiResponse<>(false, "未找到相关任务", new ArrayList<>());
            }

            logger.info("找到 {} 条相关任务记录", userMixtureTasks.size());

            // 2. 转换数据格式为List<Map<String, Object>>
            List<Map<String, Object>> resultList = new ArrayList<>();

            for (UserMixtureTask task : userMixtureTasks) {
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("taskId", task.getTaskId());
                taskInfo.put("taskName", task.getTaskName());
                taskInfo.put("assignmentInfo", task.getTaskAssignment());
                taskInfo.put("status", task.getStatus());

                // 只添加有指派人的任务
                if (task.getAcceptor() != null) {
                    taskInfo.put("assigned_to", task.getAcceptor());
                    resultList.add(taskInfo);
                }
            }

            logger.info("任务ID前缀: {} 的实验指派信息获取成功，共 {} 条记录",
                    taskIdPrefix, resultList.size());
            return new ApiResponse<>(true, "获取实验指派信息成功", resultList);
        } catch (Exception e) {
            logger.error("获取任务实验指派信息时出错: {}", e.getMessage(), e);
            return new ApiResponse<>(false, "获取实验指派信息失败: " + e.getMessage(), new ArrayList<>());
        }
    }

    /**
     * 根据任务ID获取备注信息
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
     * @param taskId       任务ID
     * @param deviceType   设备类型
     * @param deviceModel  设备型号
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

            // 添加主任务ID
            result.put("mainTaskId", taskIdPrefix);

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

            // 首先获取specimen_id和mixratio_id的映射关系
            Map<Long, Long> specimenToMixratioMap = new HashMap<>();
            try {
                String mapQuery = "SELECT specimen_id, mixratio_id FROM mixture_task WHERE task_id LIKE ? AND specimen_id IS NOT NULL AND mixratio_id IS NOT NULL";
                List<Map<String, Object>> mappings = jdbcTemplate.queryForList(mapQuery, taskIdPrefix + "%");
                logger.info("查询到 {} 条specimen_id和mixratio_id的映射关系", mappings.size());

                for (Map<String, Object> mapping : mappings) {
                    if (mapping.get("specimen_id") instanceof Number && mapping.get("mixratio_id") instanceof Number) {
                        Long specimenId = ((Number) mapping.get("specimen_id")).longValue();
                        Long mixratioId = ((Number) mapping.get("mixratio_id")).longValue();
                        specimenToMixratioMap.put(specimenId, mixratioId);
                        logger.info("映射关系: specimen_id={}, mixratio_id={}", specimenId, mixratioId);
                    }
                }
                logger.info("建立了 {} 个specimen_id到mixratio_id的映射", specimenToMixratioMap.size());
            } catch (Exception e) {
                logger.error("查询specimen_id和mixratio_id映射关系时出错: {}", e.getMessage(), e);
            }

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

                            // 记录该specimen_id对应的mixratio_id
                            Long mixratioId = specimenToMixratioMap.get(specimenId);
                            if (mixratioId != null) {
                                specimen.put("mixratio_id", mixratioId);
                                logger.info("该specimen ID {} 对应的mixratio_id: {}", specimenId, mixratioId);
                            } else {
                                logger.warn("未找到specimen ID {} 对应的mixratio_id", specimenId);
                            }
                        }
                        methodsAndRatios.addAll(specimens);
                    }
                } catch (Exception e) {
                    logger.error("查询specimen ID {} 的信息时出错: {}", specimenId, e.getMessage());
                }
            }

            // 获取配比信息
            try {
                // 从mixture_task表获取mixratio_id - 这个已经不再需要，前面已经获取了映射关系
                Set<Long> mixratioIds = new HashSet<>(specimenToMixratioMap.values());
                logger.info("通过映射关系找到 {} 个配比ID", mixratioIds.size());

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

                    // 创建mixratio_id到mix_name的映射
                    Map<Long, String> mixratioToNameMap = new HashMap<>();
                    for (Map<String, Object> mixratio : mixratios) {
                        if (mixratio.get("id") instanceof Number) {
                            Long mixratioId = ((Number) mixratio.get("id")).longValue();
                            String mixName = (String) mixratio.get("mix_name");
                            mixratioToNameMap.put(mixratioId, mixName);
                            logger.info("配比ID {} 的名称: {}", mixratioId, mixName);
                        }
                    }
                    logger.info("建立了 {} 个mixratio_id到mix_name的映射", mixratioToNameMap.size());

                    // 为每个method分配正确的配比名称
                    for (Map<String, Object> method : methodsAndRatios) {
                        if (method.get("id") instanceof Number) {
                            Long specimenId = ((Number) method.get("id")).longValue();
                            Long mixratioId = (Long) method.get("mixratio_id"); // 使用前面添加的mixratio_id

                            if (mixratioId != null && mixratioToNameMap.containsKey(mixratioId)) {
                                method.put("mix_name", mixratioToNameMap.get(mixratioId));
                                method.put("mixratio_id", mixratioId); // 确保输出包含mixratio_id
                                logger.info("为specimen_id {} 分配配比ID {} 的名称: {}",
                                        specimenId, mixratioId, mixratioToNameMap.get(mixratioId));
                            } else {
                                method.put("mix_name", "标准配比");
                                logger.info("未找到specimen_id {} 对应的配比ID {} 的名称，使用默认名称: 标准配比",
                                        specimenId, mixratioId);
                            }
                        } else {
                            method.put("mix_name", "标准配比");
                            logger.warn("specimen_id不是数字类型，使用默认名称: 标准配比");
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

            // 6. 获取任务指派信息
            try {
                // 查询当前任务的指派信息 - 从mixture_task表获取，而不是mixture_task_assignment
                List<Map<String, Object>> taskAssignments = jdbcTemplate.queryForList(
                        "SELECT task_id, task_assignment, acceptor as assigned_to, status, " +
                                "mixratio_id, specimen_id " +  // 添加mixratio_id和specimen_id字段
                                "FROM mixture_task " +
                                "WHERE task_id LIKE ? AND acceptor IS NOT NULL AND task_assignment IS NOT NULL",
                        taskIdPrefix + "%"
                );

                // 处理结果，确保所有字段都是标准格式
                List<Map<String, Object>> processedAssignments = new ArrayList<>();
                for (Map<String, Object> assignment : taskAssignments) {
                    Map<String, Object> processedAssignment = new HashMap<>();
                    processedAssignment.put("task_id", assignment.get("task_id"));
                    processedAssignment.put("taskName", assignment.get("task_name"));
                    processedAssignment.put("assignmentInfo", assignment.get("task_assignment"));
                    processedAssignment.put("assigned_to", assignment.get("assigned_to"));
                    processedAssignment.put("status", assignment.get("status"));

                    // 确保mixratio_id和specimen_id字段存在并格式正确
                    if (assignment.containsKey("mixratio_id") && assignment.get("mixratio_id") != null) {
                        processedAssignment.put("mixratio_id", assignment.get("mixratio_id"));
                    }

                    if (assignment.containsKey("specimen_id") && assignment.get("specimen_id") != null) {
                        processedAssignment.put("specimen_id", assignment.get("specimen_id"));
                    }

                    processedAssignments.add(processedAssignment);
                }

                result.put("taskAssignments", processedAssignments);
                logger.info("获取了 {} 条任务指派记录", processedAssignments.size());
            } catch (Exception e) {
                logger.error("获取任务指派信息时出错: {}", e.getMessage(), e);
                result.put("taskAssignments", new ArrayList<>());
            }

            // 构建配比ID到实验类型的映射
            try {
                // 从mixture_task表获取mixratio_id, specimen_id和task_assignment的关系
                List<Map<String, Object>> mixtureAssignments = jdbcTemplate.queryForList(
                        "SELECT mixratio_id, specimen_id, task_assignment FROM mixture_task " +
                                "WHERE task_id LIKE ? AND mixratio_id IS NOT NULL AND task_assignment IS NOT NULL",
                        taskIdPrefix + "%"
                );

                logger.info("获取到 {} 条原始任务指派记录", mixtureAssignments.size());

                // 创建一个临时结构来存储mixratio_id, specimen_id和对应的实验类型
                Map<String, Map<String, Set<String>>> ratioSpecimenExperiments = new HashMap<>();

                // 先收集所有数据来分析
                for (Map<String, Object> assignment : mixtureAssignments) {
                    Object mixratioIdObj = assignment.get("mixratio_id");
                    Object specimenIdObj = assignment.get("specimen_id");
                    String taskAssignment = (String) assignment.get("task_assignment");

                    if (mixratioIdObj != null && taskAssignment != null) {
                        // 将mixratio_id转换为字符串
                        String mixratioIdStr;
                        if (mixratioIdObj instanceof Number) {
                            mixratioIdStr = String.valueOf(((Number) mixratioIdObj).longValue());
                        } else {
                            mixratioIdStr = String.valueOf(mixratioIdObj);
                        }

                        // 获取specimen_id（如果存在）
                        String specimenIdStr = "unknown";
                        if (specimenIdObj != null) {
                            if (specimenIdObj instanceof Number) {
                                specimenIdStr = String.valueOf(((Number) specimenIdObj).longValue());
                            } else {
                                specimenIdStr = String.valueOf(specimenIdObj);
                            }
                        }

                        // 将mixratio_id和specimen_id添加到临时结构中
                        if (!ratioSpecimenExperiments.containsKey(mixratioIdStr)) {
                            ratioSpecimenExperiments.put(mixratioIdStr, new HashMap<>());
                        }

                        if (!ratioSpecimenExperiments.get(mixratioIdStr).containsKey(specimenIdStr)) {
                            ratioSpecimenExperiments.get(mixratioIdStr).put(specimenIdStr, new HashSet<>());
                        }

                        // 添加task_assignment到对应的集合中
                        ratioSpecimenExperiments.get(mixratioIdStr).get(specimenIdStr).add(taskAssignment);
                    }
                }

                // 分析并构建最终的实验指派映射
                Map<String, List<String>> ratioToExperiments = new HashMap<>();

                // 分析数据，找出每个配比正确的实验指派
                for (String mixratioId : ratioSpecimenExperiments.keySet()) {
                    Map<String, Set<String>> specimenExperiments = ratioSpecimenExperiments.get(mixratioId);

                    // 创建一个实验计数器，用于确定哪些实验是有效的
                    Map<String, Integer> experimentCounts = new HashMap<>();

                    // 统计每个实验在这个配比下出现的次数
                    for (Set<String> experiments : specimenExperiments.values()) {
                        for (String experiment : experiments) {
                            experimentCounts.put(experiment, experimentCounts.getOrDefault(experiment, 0) + 1);
                        }
                    }

                    // 找出这个配比最常分配到的实验（出现频率最高的）
                    Set<String> validExperiments = new HashSet<>();
                    for (Map.Entry<String, Integer> entry : experimentCounts.entrySet()) {
                        // 暂定规则：如果一个实验被分配给了至少一个试件，就认为是有效的
                        if (entry.getValue() > 0) {
                            validExperiments.add(entry.getKey());
                        }
                    }

                    // 将结果保存到最终映射中
                    if (!validExperiments.isEmpty()) {
                        ratioToExperiments.put(mixratioId, new ArrayList<>(validExperiments));
                        logger.info("配比ID: {} 有效的实验指派: {}", mixratioId, validExperiments);
                    }

                    // 为了兼容性，同时添加复合键映射
                    for (String specimenId : specimenExperiments.keySet()) {
                        if (!"unknown".equals(specimenId)) {
                            String compositeKey = mixratioId + "_" + specimenId;
                            Set<String> experiments = specimenExperiments.get(specimenId);
                            if (experiments != null && !experiments.isEmpty()) {
                                ratioToExperiments.put(compositeKey, new ArrayList<>(experiments));
                                logger.info("复合键 {} 的实验指派: {}", compositeKey, experiments);
                            }
                        }
                    }
                }

                // 将映射添加到结果中
                result.put("experimentAssignments", ratioToExperiments);
                logger.info("成功构建配比ID到实验类型的映射: 共 {} 个配比ID", ratioToExperiments.size());
            } catch (Exception e) {
                logger.error("构建配比ID到实验类型映射时出错: {}", e.getMessage(), e);
                result.put("experimentAssignments", new HashMap<>());
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

    /**
     * 获取指定任务ID的详细信息
     *
     * @param taskId 任务ID
     * @return 任务详细信息Map
     */
    public Map<String, Object> getMixtureTaskDataByTaskId(String taskId) {
        try {
            logger.info("获取任务ID为{}的详细信息", taskId);

            // 从taskId提取前缀部分（如果有后缀）
            String taskIdPrefix = taskId;
            if (taskId.matches(".*-\\d+$")) {
                taskIdPrefix = taskId.substring(0, taskId.lastIndexOf('-'));
                logger.info("从任务ID中提取前缀: {}", taskIdPrefix);
            }

            // 查询条件：精确匹配传入的taskId或匹配前缀下的所有任务
            String sql = "SELECT * FROM mixture_task WHERE task_id = ? OR task_id LIKE ?";
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, taskId, taskIdPrefix + "%");

            if (results.isEmpty()) {
                logger.warn("未找到任务ID={}或前缀={}的数据", taskId, taskIdPrefix);
                return new HashMap<>();
            }

            logger.info("找到{}条相关任务记录", results.size());

            // 格式化JDBC返回的结果
            Map<String, Object> result = new HashMap<>();
            Map<String, Object> taskDetails = results.get(0); // 使用第一条记录的基础信息

            // 添加基本任务信息
            for (Map.Entry<String, Object> entry : taskDetails.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                result.put(camelCase(key), value);
            }

            // 添加所有匹配的任务指派信息
            List<Map<String, Object>> taskAssignments = new ArrayList<>();
            for (Map<String, Object> task : results) {
                Map<String, Object> assignment = new HashMap<>();
                assignment.put("task_id", task.get("task_id"));
                assignment.put("taskName", task.get("task_name"));
                assignment.put("assignmentInfo", task.get("task_assignment"));
                assignment.put("status", task.get("status"));

                // 只添加有指派人的任务
                if (task.get("acceptor") != null) {
                    assignment.put("assigned_to", task.get("acceptor"));
                    taskAssignments.add(assignment);
                }
            }

            // 如果存在任务指派信息，添加到结果中
            if (!taskAssignments.isEmpty()) {
                result.put("taskAssignments", taskAssignments);
                logger.info("添加了{}条任务指派信息", taskAssignments.size());
            }

            logger.info("成功获取任务数据");
            return result;
        } catch (Exception e) {
            logger.error("获取任务详细信息时出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 获取支持的混合料任务类型列表
     *
     * @param taskType 任务类型
     * @return 支持的任务类型列表
     */
    public List<SupportMixtureTask> getSupportedMixtureTasks(String taskType) {
        // 从support_mixture_task表中查询指定类型的任务
        logger.info("获取类型为 {} 的支持任务列表", taskType);
        return supportMixtureTaskRepository.findByTaskType(taskType);
    }

    /**
     * 保存动态模量试验数据
     *
     * @param requestData 包含试验数据的请求Map
     * @return 保存结果
     */
    @Transactional
    public Map<String, Object> saveDynamicModulusTest(Map<String, Object> requestData) {
        String taskId = (String) requestData.get("taskId");
        try {
            Map<String, Object> result = new HashMap<>();


            String mixRatioId = (String) requestData.get("mixRatioId");

            logger.info("保存动态模量试验数据: taskId={}, mixRatioId={}", taskId, mixRatioId);

            // 1. 保存试验记录到dynamic_modulus_test表
            Map<String, Object> testData = new HashMap<>();
            testData.put("task_id", taskId);
            testData.put("experiment_name", "动态模量试验");
            testData.put("mix_ratio_id", mixRatioId);

            // 尝试获取配比名称
            try {
                // 将字符串类型的ID转换为整数
                Integer mixRatioIdInt = Integer.parseInt(mixRatioId);
                String sql = "SELECT mix_name FROM mixratio WHERE id = ?";
                Map<String, Object> mixRatio = jdbcTemplate.queryForMap(sql, mixRatioIdInt);
                if (mixRatio != null && mixRatio.containsKey("mix_name")) {
                    String mixRatioName = (String) mixRatio.get("mix_name");
                    testData.put("mix_ratio_name", mixRatioName);
                    testData.put("mix_ratio_display_name", mixRatioName);
                }
            } catch (Exception e) {
                logger.warn("获取配比名称失败: {}", e.getMessage());
                // 设置默认值，确保事务可以继续
                testData.put("mix_ratio_name", "配比" + mixRatioId);
                testData.put("mix_ratio_display_name", "配比" + mixRatioId);
            }

            testData.put("created_at", new Timestamp(System.currentTimeMillis()));
            testData.put("updated_at", new Timestamp(System.currentTimeMillis()));

            // 执行插入并获取生成的测试ID
            String insertSql = "INSERT INTO dynamic_modulus_test (task_id, experiment_name, mix_ratio_id, mix_ratio_name, mix_ratio_display_name, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

            Long testId = jdbcTemplate.queryForObject(insertSql,
                    Long.class,
                    testData.get("task_id"),
                    testData.get("experiment_name"),
                    testData.get("mix_ratio_id"),
                    testData.get("mix_ratio_name"),
                    testData.get("mix_ratio_display_name"),
                    testData.get("created_at"),
                    testData.get("updated_at"));

            if (testId == null) {
                throw new RuntimeException("无法获取插入的测试ID");
            }

            result.put("testId", testId);

            // 2. 处理试件数据
            if (requestData.containsKey("specimens")) {
                List<Map<String, Object>> specimens = (List<Map<String, Object>>) requestData.get("specimens");
                for (Map<String, Object> specimen : specimens) {
                    Integer specimenNumber = (Integer) specimen.get("specimenNumber");
                    Float diameter = specimen.get("diameter") != null ? ((Number) specimen.get("diameter")).floatValue() : null;
                    Float height = specimen.get("height") != null ? ((Number) specimen.get("height")).floatValue() : null;
                    Float bulkDensity = specimen.get("bulkDensity") != null ? ((Number) specimen.get("bulkDensity")).floatValue() : null;
                    Float airVoidContent = specimen.get("airVoidContent") != null ? ((Number) specimen.get("airVoidContent")).floatValue() : null;

                    // 保存试件信息到dynamic_modulus_specimen表
                    String specimenSql = "INSERT INTO dynamic_modulus_specimen (test_id, specimen_number, diameter, height, bulk_density, air_void_content, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(specimenSql,
                            testId,
                            specimenNumber,
                            diameter,
                            height,
                            bulkDensity,
                            airVoidContent,
                            new Timestamp(System.currentTimeMillis()));
                }
            }

            // 3. 处理温度数据
            Map<Float, Long> temperatureIdMap = new HashMap<>(); // 用于存储温度值到数据库ID的映射

            if (requestData.containsKey("temperatures")) {
                List<Map<String, Object>> temperatures = (List<Map<String, Object>>) requestData.get("temperatures");
                for (Map<String, Object> tempData : temperatures) {
                    Float temperature = tempData.get("temperature") != null ?
                            ((Number) tempData.get("temperature")).floatValue() : null;
                    Integer tempOrder = tempData.get("temperatureOrder") != null ?
                            ((Number) tempData.get("temperatureOrder")).intValue() : null;

                    // 保存温度信息到dynamic_modulus_temperature表
                    String tempSql = "INSERT INTO dynamic_modulus_temperature (test_id, temperature, temperature_order, created_at) " +
                            "VALUES (?, ?, ?, ?) RETURNING id";

                    Long temperatureId = jdbcTemplate.queryForObject(tempSql,
                            Long.class,
                            testId,
                            temperature,
                            tempOrder,
                            new Timestamp(System.currentTimeMillis()));

                    if (temperatureId != null) {
                        temperatureIdMap.put(temperature, temperatureId);
                    }
                }
            }

            // 4. 处理测量数据
            if (requestData.containsKey("measurements")) {
                List<Map<String, Object>> measurements = (List<Map<String, Object>>) requestData.get("measurements");
                for (Map<String, Object> measurement : measurements) {
                    Integer specimenId = (Integer) measurement.get("specimenId");
                    Float temperature = measurement.get("temperature") != null ?
                            ((Number) measurement.get("temperature")).floatValue() : null;
                    Float frequency = measurement.get("frequency") != null ?
                            ((Number) measurement.get("frequency")).floatValue() : null;
                    Integer cycleCount = measurement.get("cycleCount") != null ?
                            ((Number) measurement.get("cycleCount")).intValue() : null;
                    Float dynamicModulus = measurement.get("dynamicModulus") != null ?
                            ((Number) measurement.get("dynamicModulus")).floatValue() : null;
                    Float phaseAngle = measurement.get("phaseAngle") != null ?
                            ((Number) measurement.get("phaseAngle")).floatValue() : null;
                    Float axialStress = measurement.get("axialStress") != null ?
                            ((Number) measurement.get("axialStress")).floatValue() : null;
                    Float axialStrain = measurement.get("axialStrain") != null ?
                            ((Number) measurement.get("axialStrain")).floatValue() : null;
                    Float permanentDeformation = measurement.get("permanentDeformation") != null ?
                            ((Number) measurement.get("permanentDeformation")).floatValue() : null;

                    // 获取温度ID
                    Long temperatureId = temperatureIdMap.get(temperature);

                    // 保存测量数据到dynamic_modulus_measurement表
                    String measurementSql = "INSERT INTO dynamic_modulus_measurement " +
                            "(test_id, temperature_id, frequency, cycle_count, dynamic_modulus, phase_angle, " +
                            "axial_stress, axial_strain, permanent_deformation, test_date, is_valid, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(measurementSql,
                            testId,                          // 添加testId
                            temperatureId,
                            frequency,
                            cycleCount,
                            dynamicModulus,
                            phaseAngle,
                            axialStress,
                            axialStrain,
                            permanentDeformation,
                            new Timestamp(System.currentTimeMillis()), // test_date
                            true, // is_valid
                            new Timestamp(System.currentTimeMillis())); // created_at
                }
            }

            logger.info("成功保存动态模量试验数据: testId={}", testId);
            result.put("success", true);
            result.put("message", "动态模量试验数据保存成功");

            // 更新任务状态
            String taskAssignment = "动态模量试验";
            updateExperimentTaskStatus(taskId, taskAssignment);

            return result;
        } catch (Exception e) {
            logger.error("保存动态模量试验数据时出错: {}", e.getMessage(), e);
            throw new RuntimeException("保存动态模量试验数据失败: " + e.getMessage(), e);
        }
    }


    /**
     * 保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据
     *
     * @param requestData 包含试验数据的请求Map
     * @return 保存结果
     */
    public Map<String, String> saveDirectStretchingFatigueTestData(Map<String, Object> requestData) {
        try {
            logger.info("处理沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据: {}", requestData);

            // 初始化结果MAP
            Map<String, String> result = new HashMap<>();

            // 从请求中提取基本参数
            String taskId = (String) requestData.get("taskId");
            String mixRatioId = (String) requestData.get("mixRatioId");
            Map<String, Object> testInfo = (Map<String, Object>) requestData.get("testInfo");
            List<Map<String, Object>> specimens = (List<Map<String, Object>>) requestData.get("specimens");

            // 1. 保存测试基本信息
            String testDate = (String) testInfo.get("testDate");
            String operator = (String) testInfo.get("operator");
            String equipmentId = (String) testInfo.get("equipmentId");
            String notes = (String) testInfo.get("notes");

            // 提取测试温度（如果有）
            BigDecimal testTemperature = null;
            if (requestData.get("testTemperature") != null && !requestData.get("testTemperature").toString().isEmpty()) {
                testTemperature = new BigDecimal(requestData.get("testTemperature").toString());
            }

            // 插入测试记录
            String insertTestSql = "INSERT INTO direct_stretching_fatigue_test " +
                    "(test_id, task_id, mix_ratio_id, test_temperature, test_time, " +
                    "operator, test_equipment, test_method, test_standard, remarks, " +
                    "create_time, update_time) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            jdbcTemplate.update(insertTestSql,
                    UUID.randomUUID().toString(),
                    taskId,
                    mixRatioId,
                    testTemperature,
                    operator,
                    equipmentId,
                    "直接拉伸循环疲劳测黏弹损伤试验",
                    "标准试验",
                    notes);

            // 2. 保存试件数据
            for (Map<String, Object> specimen : specimens) {
                String specimenId = (String) specimen.get("specimenId");
                String height = (String) specimen.get("height");
                String diameter = (String) specimen.get("diameter");

                // 保存试件基本信息
                String specimenSql = "INSERT INTO direct_stretching_fatigue_specimens (test_id, specimen_id, height, diameter, created_at) " +
                        "VALUES (?, ?, ?, ?, ?)";

                jdbcTemplate.update(specimenSql,
                        UUID.randomUUID().toString(),
                        specimenId,
                        height != null && !height.isEmpty() ? Float.parseFloat(height) : null,
                        diameter != null && !diameter.isEmpty() ? Float.parseFloat(diameter) : null,
                        new Timestamp(System.currentTimeMillis()));

                // 3. 保存动态模量数据
                Map<String, Map<String, String>> modulusData = (Map<String, Map<String, String>>) specimen.get("modulusData");
                for (Map.Entry<String, Map<String, String>> entry : modulusData.entrySet()) {
                    String stage = entry.getKey(); // initial或final
                    Map<String, String> data = entry.getValue();

                    String modulusSql = "INSERT INTO direct_stretching_modulus_data " +
                            "(specimen_id, stage, dynamic_modulus, cycle_count, phase_angle, force_level, " +
                            "equilibrium_strain, temperature, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(modulusSql,
                            specimenId,
                            stage,
                            data.get("dynamicModulus") != null && !data.get("dynamicModulus").isEmpty() ?
                                    Float.parseFloat(data.get("dynamicModulus")) : null,
                            data.get("cycleCount") != null && !data.get("cycleCount").isEmpty() ?
                                    Integer.parseInt(data.get("cycleCount")) : null,
                            data.get("phaseAngle") != null && !data.get("phaseAngle").isEmpty() ?
                                    Float.parseFloat(data.get("phaseAngle")) : null,
                            data.get("forceLevel") != null && !data.get("forceLevel").isEmpty() ?
                                    Float.parseFloat(data.get("forceLevel")) : null,
                            data.get("uniformStrain") != null && !data.get("uniformStrain").isEmpty() ?
                                    Float.parseFloat(data.get("uniformStrain")) : null,
                            data.get("temperature") != null && !data.get("temperature").isEmpty() ?
                                    Float.parseFloat(data.get("temperature")) : null,
                            new Timestamp(System.currentTimeMillis()));
                }

                // 4. 保存疲劳数据
                Map<String, Map<String, String>> fatigueData = (Map<String, Map<String, String>>) specimen.get("fatigueData");
                for (Map.Entry<String, Map<String, String>> entry : fatigueData.entrySet()) {
                    String stage = entry.getKey(); // initial或final
                    Map<String, String> data = entry.getValue();

                    String fatigueSql = "INSERT INTO direct_stretching_fatigue_data " +
                            "(specimen_id, stage, dynamic_modulus, cycle_count, phase_angle, force_level, " +
                            "equilibrium_strain, temperature, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(fatigueSql,
                            specimenId,
                            stage,
                            data.get("dynamicModulus") != null && !data.get("dynamicModulus").isEmpty() ?
                                    Float.parseFloat(data.get("dynamicModulus")) : null,
                            data.get("cycleCount") != null && !data.get("cycleCount").isEmpty() ?
                                    Integer.parseInt(data.get("cycleCount")) : null,
                            data.get("phaseAngle") != null && !data.get("phaseAngle").isEmpty() ?
                                    Float.parseFloat(data.get("phaseAngle")) : null,
                            data.get("forceLevel") != null && !data.get("forceLevel").isEmpty() ?
                                    Float.parseFloat(data.get("forceLevel")) : null,
                            data.get("equilibrium_strain") != null && !data.get("equilibrium_strain").isEmpty() ?
                                    Float.parseFloat(data.get("equilibrium_strain")) : null,
                            data.get("temperature") != null && !data.get("temperature").isEmpty() ?
                                    Float.parseFloat(data.get("temperature")) : null,
                            new Timestamp(System.currentTimeMillis()));
                }
            }

            logger.info("成功保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据");
            result.put("success", "true");
            result.put("message", "沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据保存成功");

            // 更新任务状态
            String taskAssignment = "沥青混合料直接拉伸循环疲劳测黏弹损伤试验";
            updateExperimentTaskStatus(taskId, taskAssignment);

            return result;
        } catch (Exception e) {
            logger.error("保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据时出错: {}", e.getMessage(), e);
            throw new RuntimeException("保存沥青混合料直接拉伸循环疲劳测黏弹损伤试验数据失败: " + e.getMessage(), e);
        }
    }


    /**
     * 保存沥青混合料四点弯曲疲劳寿命试验数据
     *
     * @param requestData 包含试验数据的请求Map
     * @return 保存结果
     */
    public Map<String, String> saveFourPointFatigueTestData(Map<String, Object> requestData) {
        logger.info("处理沥青混合料四点弯曲疲劳寿命试验数据: {}", requestData);

        try {
            // 初始化结果MAP
            Map<String, String> result = new HashMap<>();

            // 从请求中提取基本参数
            String taskId = (String) requestData.get("taskId");
            String mixRatioId = String.valueOf(requestData.get("mixRatioId"));

            // 获取测试数据信息
            Map<String, Object> testData = (Map<String, Object>) requestData.get("testData");
            String experimentName = testData != null ? (String) testData.get("experimentName") : null;

            // 其他测试参数可能来自顶层或testData，根据实际前端结构调整
            // 如果这些值在前端没有提供，设置默认值或null
            Timestamp testDate = new Timestamp(System.currentTimeMillis());
            String operator = "未知";  // 默认值
            String equipmentId = "未知";  // 默认值
            Double temperature = 25.0;  // 默认值
            Double frequency = 10.0;  // 默认值
            String loadingMode = "默认控制";  // 默认值
            String notes = experimentName != null ? experimentName : "沥青混合料四点弯曲疲劳寿命试验";
            Double testTemperature = (Double) requestData.get("testTemperature");

            // 插入测试记录
            String testSql = "INSERT INTO mixture_four_point_bending_test" +
                    "(task_id, mix_ratio_id, mix_ratio_name, experiment_name, mix_temperature, mix_speed, mix_time, " +
                    "compaction_method, test_date, operator, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            // 从testData或requestData中获取数据或使用默认值
            String mixRatioName = ""; // 如果前端发送了这个数据，从适当位置获取
            Double mixTemperature = null; // 如果前端发送了这个数据，从适当位置获取
            Double mixSpeed = null; // 如果前端发送了这个数据，从适当位置获取
            Double mixTime = null; // 如果前端发送了这个数据，从适当位置获取
            String compactionMethod = ""; // 如果前端发送了这个数据，从适当位置获取

            Long testId = jdbcTemplate.queryForObject(testSql,
                    Long.class,
                    taskId,
                    mixRatioId,
                    mixRatioName,
                    mixTemperature,
                    mixSpeed,
                    mixTime,
                    compactionMethod,
                    testDate,
                    operator,
                    new Timestamp(System.currentTimeMillis()));

            // 2. 保存试件数据
            for (Map<String, Object> specimen : (List<Map<String, Object>>) requestData.get("specimens")) {
                Integer specimenNumber = (Integer) specimen.get("specimenNumber"); // 注意这里改为specimenNumber
                Double height = parseDoubleValue(specimen.get("height"));
                Double width = parseDoubleValue(specimen.get("width"));
                Double length = parseDoubleValue(specimen.get("length"));

                // 从specimen中获取其他字段，或设置为null/默认值
                Double spanMm = parseDoubleValue(specimen.get("spanMm")); // 可以从specimen中获取或设置默认值
                Double strainRange = parseDoubleValue(specimen.get("strainRange")); // 可以从specimen中获取或设置默认值
                Double frequencyHz = parseDoubleValue(specimen.get("frequencyHz")); // 可以从specimen中获取或设置默认值
                Double specimenTemperature = parseDoubleValue(specimen.get("testTemperature")); // 可以从specimen中获取或设置默认值
                Double fatigueLife = parseDoubleValue(specimen.get("fatigueLife")); // 可以从specimen中获取或设置默认值

                // 保存试件基本信息和测试结果
                String specimenSql = "INSERT INTO mixture_four_point_bending_specimen" +
                        "(test_id, specimen_number, length_mm, width_mm, height_mm, " +
                        "span_mm, strain_range, frequency_hz, test_temperature, fatigue_life, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                Long specimenDbId = jdbcTemplate.queryForObject(specimenSql,
                        Long.class,
                        testId,
                        specimenNumber,
                        length,
                        width,
                        height,
                        spanMm,
                        strainRange,
                        frequencyHz,
                        specimenTemperature,
                        fatigueLife,
                        new Timestamp(System.currentTimeMillis()));

                // 3. 保存测试结果数据（如果有）
                List<Map<String, Object>> results = (List<Map<String, Object>>) specimen.get("results");
                if (results != null && !results.isEmpty()) {
                    for (Map<String, Object> resultItem : results) {
                        String resultType = resultItem.get("resultType") != null ?
                                String.valueOf(resultItem.get("resultType")) : null;
                        String resultTypeDisplayName = resultItem.get("resultTypeDisplayName") != null ?
                                String.valueOf(resultItem.get("resultTypeDisplayName")) : null;
                        String resultTypeEnglishName = resultItem.get("resultTypeEnglishName") != null ?
                                String.valueOf(resultItem.get("resultTypeEnglishName")) : null;
                        String resultTypeUnit = resultItem.get("resultTypeUnit") != null ?
                                String.valueOf(resultItem.get("resultTypeUnit")) : null;
                        Integer resultIndex = resultItem.get("resultIndex") != null ?
                                Integer.valueOf(resultItem.get("resultIndex").toString()) : null;
                        Double initialValue = parseDoubleValue(resultItem.get("initialValue"));
                        Double currentValue = parseDoubleValue(resultItem.get("currentValue"));

                        String resultSql = "INSERT INTO mixture_four_point_bending_result" +
                                "(specimen_id, result_type, result_type_display_name, result_type_english_name, " +
                                "result_type_unit, result_index, initial_value, current_value, created_at) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                        jdbcTemplate.update(resultSql,
                                specimenDbId,
                                resultType,
                                resultTypeDisplayName,
                                resultTypeEnglishName,
                                resultTypeUnit,
                                resultIndex,
                                initialValue,
                                currentValue,
                                new Timestamp(System.currentTimeMillis()));
                    }
                }
            }

            logger.info("成功保存沥青混合料弯曲试验数据: testId={}", testId);
            result.put("success", "true");
            result.put("message", "沥青混合料弯曲试验数据保存成功");

            // 更新任务状态
            String taskAssignment = "沥青混合料四点弯曲疲劳寿命试验";
            updateExperimentTaskStatus(taskId, taskAssignment);

            return result;
        } catch (Exception e) {
            logger.error("保存沥青混合料四点弯曲疲劳寿命试验数据时出错: {}", e.getMessage(), e);
            throw new RuntimeException("保存沥青混合料四点弯曲疲劳寿命试验数据失败: " + e.getMessage(), e);
        }
    }


    /**
     * 保存劈裂试验数据
     *
     * @param requestData 前端传入的测试数据
     * @return 保存结果
     */
    public Map<String, String> saveSplittingTestData(Map<String, Object> requestData) {
        Map<String, String> result = new HashMap<>();
        String taskId = (String) requestData.get("taskId");
        try {
            // 提取基本信息

            String mixRatioId = (String) requestData.get("mixRatioId");
            String testId = (String) requestData.get("testId");
            String operator = (String) requestData.get("operator");
            String testEquipment = (String) requestData.get("testEquipment");
            String testMethod = (String) requestData.get("testMethod");
            String testStandard = (String) requestData.get("testStandard");
            String remarks = (String) requestData.get("remarks");

            // 提取测试温度（如果有）
            BigDecimal testTemperature = null;
            if (requestData.get("testTemperature") != null && !requestData.get("testTemperature").toString().isEmpty()) {
                testTemperature = new BigDecimal(requestData.get("testTemperature").toString());
            }

            // 插入测试记录
            String insertTestSql = "INSERT INTO mixture_splitting_test " +
                    "(test_id, task_id, mix_ratio_id, test_temperature, test_time, " +
                    "operator, test_equipment, test_method, test_standard, remarks, " +
                    "create_time, update_time) " +
                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            jdbcTemplate.update(insertTestSql,
                    testId,
                    taskId,
                    mixRatioId,
                    testTemperature,
                    operator,
                    testEquipment,
                    testMethod,
                    testStandard,
                    remarks);

            // 处理试件数据
            List<Map<String, Object>> specimens = (List<Map<String, Object>>) requestData.get("specimens");
            if (specimens != null && !specimens.isEmpty()) {
                for (Map<String, Object> specimenData : specimens) {
                    String specimenId = (String) specimenData.get("specimenId");
                    Integer specimenNumber = specimenData.get("specimenNumber") != null ?
                            Integer.parseInt(specimenData.get("specimenNumber").toString()) : null;

                    // 提取试件尺寸
                    BigDecimal diameter = getBigDecimalValue(specimenData, "diameter");
                    BigDecimal height = getBigDecimalValue(specimenData, "height");

                    // 提取p值
                    BigDecimal p1Value = getBigDecimalValue(specimenData, "p1Value");
                    BigDecimal p2Value = getBigDecimalValue(specimenData, "p2Value");
                    BigDecimal p3Value = getBigDecimalValue(specimenData, "p3Value");
                    // 不再直接使用前端传过来的pAverage，而是在后端计算
                    //BigDecimal pAverage = getBigDecimalValue(specimenData, "pAverage");
                    BigDecimal pAverage = calculateAverage(p1Value, p2Value, p3Value);

                    // 提取x值
                    BigDecimal x1Value = getBigDecimalValue(specimenData, "x1Value");
                    BigDecimal x2Value = getBigDecimalValue(specimenData, "x2Value");
                    BigDecimal x3Value = getBigDecimalValue(specimenData, "x3Value");
                    BigDecimal xAverage = getBigDecimalValue(specimenData, "xAverage");

                    // 提取计算结果
                    BigDecimal poissonRatio = getBigDecimalValue(specimenData, "poissonRatio");
                    BigDecimal tensileStrength = getBigDecimalValue(specimenData, "tensileStrength");
                    BigDecimal failureStrain = getBigDecimalValue(specimenData, "failureStrain");
                    BigDecimal stiffnessModulus = getBigDecimalValue(specimenData, "stiffnessModulus");

                    // 插入试件记录
                    String insertSpecimenSql = "INSERT INTO mixture_splitting_test_specimen " +
                            "(specimen_id, test_id, specimen_number, diameter, height, " +
                            "p1_value, p2_value, p3_value, p_average, " +
                            "x1_value, x2_value, x3_value, x_average, " +
                            "poisson_ratio, tensile_strength, failure_strain, stiffness_modulus) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    jdbcTemplate.update(insertSpecimenSql,
                            specimenId,
                            testId,
                            specimenNumber,
                            diameter,
                            height,
                            p1Value,
                            p2Value,
                            p3Value,
                            pAverage,
                            x1Value,
                            x2Value,
                            x3Value,
                            xAverage,
                            poissonRatio,
                            tensileStrength,
                            failureStrain,
                            stiffnessModulus);
                }
            }

            result.put("success", "true");
            result.put("message", "劈裂试验数据保存成功");
            result.put("testId", testId);

        } catch (Exception e) {
            log.error("保存劈裂试验数据失败", e);
            result.put("success", "false");
            result.put("message", "保存失败: " + e.getMessage());
        }

        // 更新任务状态
        String taskAssignment = "沥青混合料劈裂试验";
        updateExperimentTaskStatus(taskId, taskAssignment);

        return result;
    }


    /**
     * 保存单轴压缩试验数据
     *
     * @param requestData 前端传入的测试数据
     * @return 保存结果
     */
    public Map<String, String> saveUniaxialCompressionTestData(Map<String, Object> requestData) {
        Map<String, String> result = new HashMap<>();
        String taskId = (String) requestData.get("taskId");
        try {
            // 提取基本信息

            String mixRatioId = (String) requestData.get("mixRatioId");
            Float testTemperature = requestData.get("testTemperature") != null ?
                    Float.parseFloat(requestData.get("testTemperature").toString()) : null;

            // 生成测试ID
            String testId = UUID.randomUUID().toString();

            // 插入测试记录
            String insertTestSql = "INSERT INTO mixture_uniaxial_compression_test " +
                    "(test_id, task_id, mix_ratio_id, mix_ratio_name, compaction_method, " +
                    "mixing_temperature, mixing_speed, mixing_time, test_date, test_temperature, " +
                    "average_force, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            // 获取新增字段
            String mixRatioName = (String) requestData.get("mixRatioName");
            String compactionMethod = (String) requestData.get("compactionMethod");
            Float mixingTemperature = requestData.get("mixingTemperature") != null ?
                    Float.parseFloat(requestData.get("mixingTemperature").toString()) : null;
            Float mixingSpeed = requestData.get("mixingSpeed") != null ?
                    Float.parseFloat(requestData.get("mixingSpeed").toString()) : null;
            Float mixingTime = requestData.get("mixingTime") != null ?
                    Float.parseFloat(requestData.get("mixingTime").toString()) : null;
            String testDate = (String) requestData.get("testDate");
            Float averageForce = requestData.get("averageForce") != null ?
                    Float.parseFloat(requestData.get("averageForce").toString()) : null;

            // 更新SQL参数绑定
            jdbcTemplate.update(insertTestSql, testId, taskId, mixRatioId, mixRatioName, compactionMethod,
                    mixingTemperature, mixingSpeed, mixingTime, testDate, testTemperature,
                    averageForce);

            // 记录收到的数据
            logger.info("收到单轴压缩试验数据，测试ID: {}, 任务ID: {}, 配比ID: {}", testId, taskId, mixRatioId);

            // 获取并处理试件数据
            List<Map<String, Object>> specimens = (List<Map<String, Object>>) requestData.get("specimens");
            logger.info("试件数量: {}", specimens != null ? specimens.size() : 0);

            // 获取并处理试件数据
            if (specimens != null) {
                for (Map<String, Object> specimen : specimens) {
                    Integer specimenNumber = (Integer) specimen.get("specimenNumber");
                    Float diameter = specimen.get("diameter") != null ?
                            Float.parseFloat(specimen.get("diameter").toString()) : null;
                    Float height = specimen.get("height") != null ?
                            Float.parseFloat(specimen.get("height").toString()) : null;

                    // 插入试件记录
                    String specimenId = UUID.randomUUID().toString();
                    String insertSpecimenSql = "INSERT INTO mixture_uniaxial_compression_specimen " +
                            "(specimen_id, test_id, specimen_number, diameter, height, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

                    jdbcTemplate.update(insertSpecimenSql, specimenId, testId, specimenNumber, diameter, height);

                    // 处理P值数据
                    List<?> pValuesList = (List<?>) specimen.get("pValues");
                    if (pValuesList != null && !pValuesList.isEmpty()) {
                        for (int i = 0; i < pValuesList.size(); i++) {
                            Object pValueObj = pValuesList.get(i);
                            if (pValueObj != null) {
                                Float pValue = Float.parseFloat(pValueObj.toString());
                                String insertPValueSql = "INSERT INTO mixture_uniaxial_compression_p_values " +
                                        "(specimen_id, p_index, p_value, created_at, updated_at) " +
                                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

                                jdbcTemplate.update(insertPValueSql,
                                        specimenId,
                                        i + 1,
                                        pValue);
                            }
                        }
                    }

                    // 处理UTM数据
                    List<Map<String, Object>> utmDataList = (List<Map<String, Object>>) specimen.get("utmDataList");

                    // 如果直接列表为空，尝试从嵌套结构中获取
                    if (utmDataList != null && !utmDataList.isEmpty()) {
                        // 处理多个压力级别的UTM数据
                        logger.info("开始处理UTM数据，共 {} 项", utmDataList.size());
                        int dataCount = 0;
                        for (Map<String, Object> utmData : utmDataList) {
                            // 记录原始数据
                            logger.info("UTM数据项 #{}: {}", dataCount++, utmData);
                            
                            // 获取压力级别
                            String pressureLevel = (String) utmData.get("pressureLevel");
                            logger.info("压力级别: {}", pressureLevel);
                    
                            // 获取UTM数据值，添加数据验证日志
                            Object maxForceObj = utmData.get("max_force");  // 使用前端传递的字段名称
                            Float maxForce = null;
                            if (maxForceObj != null) {
                                try {
                                    maxForce = Float.parseFloat(maxForceObj.toString());
                                    logger.info("max_force: {} -> {}", maxForceObj, maxForce);
                                } catch (Exception e) {
                                    logger.error("解析max_force失败: {} - {}", maxForceObj, e.getMessage());
                                }
                            } else {
                                logger.warn("maxForceKn为null");
                            }
                            
                            Object minForceObj = utmData.get("min_force");  // 使用前端传递的字段名称
                            Float minForce = null;
                            if (minForceObj != null) {
                                try {
                                    minForce = Float.parseFloat(minForceObj.toString());
                                    logger.info("min_force: {} -> {}", minForceObj, minForce);
                                } catch (Exception e) {
                                    logger.error("解析min_force失败: {} - {}", minForceObj, e.getMessage());
                                }
                            } else {
                                logger.warn("minForceN为null");
                            }
                            
                            Object workRatioObj = utmData.get("work_ratio");  // 使用前端传递的字段名称
                            Float workRatio = null;
                            if (workRatioObj != null) {
                                try {
                                    workRatio = Float.parseFloat(workRatioObj.toString());
                                    logger.info("work_ratio: {} -> {}", workRatioObj, workRatio);
                                } catch (Exception e) {
                                    logger.error("解析work_ratio失败: {} - {}", workRatioObj, e.getMessage());
                                }
                            } else {
                                logger.warn("stressDevKpa为null");
                            }
                            
                            Object displacementObj = utmData.get("displacement");
                            Float displacement = null;
                            if (displacementObj != null) {
                                try {
                                    displacement = Float.parseFloat(displacementObj.toString());
                                    logger.info("displacement: {} -> {}", displacementObj, displacement);
                                } catch (Exception e) {
                                    logger.error("解析displacement失败: {} - {}", displacementObj, e.getMessage());
                                }
                            }
                            
                            Object strainObj = utmData.get("strain");
                            Float strain = null;
                            if (strainObj != null) {
                                try {
                                    strain = Float.parseFloat(strainObj.toString());
                                    logger.info("strain: {} -> {}", strainObj, strain);
                                } catch (Exception e) {
                                    logger.error("解析strain失败: {} - {}", strainObj, e.getMessage());
                                }
                            }
                            
                            Object reboundModulusObj = utmData.get("rebound_modulus");
                            Float reboundModulus = null;
                            if (reboundModulusObj != null) {
                                try {
                                    reboundModulus = Float.parseFloat(reboundModulusObj.toString());
                                    logger.info("rebound_modulus: {} -> {}", reboundModulusObj, reboundModulus);
                                } catch (Exception e) {
                                    logger.error("解析rebound_modulus失败: {} - {}", reboundModulusObj, e.getMessage());
                                }
                            }
                            
                            Object temperatureObj = utmData.get("temperature");
                            Float temperature = null;
                            if (temperatureObj != null) {
                                try {
                                    temperature = Float.parseFloat(temperatureObj.toString());
                                    logger.info("temperature: {} -> {}", temperatureObj, temperature);
                                } catch (Exception e) {
                                    logger.error("解析temperature失败: {} - {}", temperatureObj, e.getMessage());
                                }
                            }
                    
                            // 汇总日志
                            logger.info("UTM数据解析结果: pressureLevel={}, maxForce={}, minForce={}, workRatio={}, displacement={}, strain={}, reboundModulus={}, temperature={}",
                                    pressureLevel, maxForce, minForce, workRatio, displacement, strain, reboundModulus, temperature);
                    
                            // 插入数据库 (使用压力级别)
                            String insertUtmDataSql = "INSERT INTO mixture_uniaxial_compression_uts028_data " +
                                    "(specimen_id, pressure_level, max_force, min_force, work_ratio, displacement, " +
                                    "strain, rebound_modulus, temperature, created_at, updated_at) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
                    
                            jdbcTemplate.update(insertUtmDataSql, specimenId, pressureLevel, maxForce, minForce, workRatio,
                                    displacement, strain, reboundModulus, temperature);
                        }
                    } 
                    
                    else {
                        // 向下兼容处理单个UTM数据对象
                        Map<String, Object> utmData = (Map<String, Object>) specimen.get("utmData");
                        if (utmData != null) {
                            // 提取旧格式数据
                            Float maxForce = utmData.get("maxForce") != null ?
                                    Float.parseFloat(utmData.get("maxForce").toString()) : null;
                            Float minForce = utmData.get("minForce") != null ?
                                    Float.parseFloat(utmData.get("minForce").toString()) : null;
                            Float workRatio = utmData.get("workRatio") != null ?
                                    Float.parseFloat(utmData.get("workRatio").toString()) : null;
                            Float displacement = utmData.get("displacement") != null ?
                                    Float.parseFloat(utmData.get("displacement").toString()) : null;
                            Float strain = utmData.get("strain") != null ?
                                    Float.parseFloat(utmData.get("strain").toString()) : null;
                            Float reboundModulus = utmData.get("reboundModulus") != null ?
                                    Float.parseFloat(utmData.get("reboundModulus").toString()) : null;
                            Float temperature = utmData.get("temperature") != null ?
                                    Float.parseFloat(utmData.get("temperature").toString()) : null;

                            // 使用默认0.1P压力级别插入数据
                            String insertUtmDataSql = "INSERT INTO mixture_uniaxial_compression_uts028_data " +
                                    "(specimen_id, pressure_level, max_force, min_force, work_ratio, displacement, " +
                                    "strain, rebound_modulus, temperature, created_at, updated_at) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

                            jdbcTemplate.update(insertUtmDataSql, specimenId, "0.1P", maxForce, minForce, workRatio,
                                    displacement, strain, reboundModulus, temperature);
                        }
                    }
                }
            }

            // 记录日志
            logger.info("成功保存单轴压缩试验数据，测试ID: {}", testId);

            // 返回结果
            result.put("success", "true");
            result.put("testId", testId);

        } catch (Exception e) {
            logger.error("保存单轴压缩试验数据时出错: {}", e.getMessage(), e);
            result.put("success", "false");
            result.put("error", e.getMessage());
            throw e;
            // 更新任务状态

        }

        String taskAssignment = "沥青混合料单轴压缩试验(圆柱体法)";
        updateExperimentTaskStatus(taskId, taskAssignment);

        return result;
    }

    /**
     * 获取混合料任务的测试状态
     *
     * @param taskId 任务ID
     * @return 测试状态
     */
    public String getTestingStatus(String taskId) {
        try {
            logger.info("获取任务ID为 {} 的测试状态", taskId);

            // 查找任务记录
            List<Map<String, Object>> tasks = mixtureTaskRepository.findAllByTaskIdPrefixNative(taskId);

            if (tasks.isEmpty()) {
                logger.warn("未找到任务ID为 {} 的记录", taskId);
                return "unknown";
            }

            // 检查任务的测试状态
            for (Map<String, Object> task : tasks) {
                String testingStatus = (String) task.get("testing_status");

                // 如果任何一个任务的testing_status为"finished"，返回"finished"
                if ("finished".equals(testingStatus)) {
                    logger.info("任务 {} 的测试状态为: finished", taskId);
                    return "finished";
                }
            }

            // 如果没有任何任务的testing_status为"finished"，返回"unfinished"
            logger.info("任务 {} 的测试状态为: unfinished", taskId);
            return "unfinished";
        } catch (Exception e) {
            logger.error("获取测试状态时发生错误", e);
            return "error";
        }
    }


    /**
     * 从Map中安全获取BigDecimal值
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) != null && !data.get(key).toString().isEmpty()) {
            try {
                return new BigDecimal(data.get(key).toString());
            } catch (NumberFormatException e) {
                log.warn("无法解析BigDecimal值: " + key + " = " + data.get(key), e);
            }
        }
        return null;
    }

    /**
     * 计算平均值
     *
     * @param values
     * @return
     */
    private BigDecimal calculateAverage(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                sum = sum.add(value);
            }
        }

        // 更新为
        return sum.divide(new BigDecimal(values.length), RoundingMode.HALF_UP);
    }

    /**
     * 更新实验任务状态
     *
     * @param taskId         任务ID
     * @param taskAssignment 实验类型
     */
    private void updateExperimentTaskStatus(String taskId, String taskAssignment) {
        try {
            // 提取任务前缀（如果任务ID包含连字符）
            String taskIdPrefix = taskId;
            int dashIndex = taskId.indexOf('-');
            if (dashIndex > 0) {
                taskIdPrefix = taskId.substring(0, dashIndex);
            }

            // 使用JdbcTemplate直接更新数据库
            String sql = "UPDATE mixture_task SET testing_status = 'finished', status = 'COMPLETE' WHERE task_id = ? AND task_assignment = ?";
            // 使用taskId进行精确匹配
            int updatedRows = jdbcTemplate.update(sql, taskId, taskAssignment);

            if (updatedRows == 0) {
                logger.warn("未找到匹配的任务(精确匹配)，尝试使用前缀匹配");
                // 如果精确匹配未成功，尝试使用前缀匹配
                sql = "UPDATE mixture_task SET testing_status = 'finished', status = 'COMPLETE' WHERE task_id LIKE ? AND task_assignment = ?";
                updatedRows = jdbcTemplate.update(sql, taskIdPrefix + "%", taskAssignment);
            }

            if (updatedRows > 0) {
                logger.info("成功更新任务ID: {} 的实验类型: {} 的状态为finished/COMPLETE，影响行数: {}", taskId, taskAssignment, updatedRows);
            } else {
                logger.warn("没有找到匹配的任务记录: task_id={}, task_assignment={}", taskId, taskAssignment);
                // 输出可能的任务分配值，以便调试
                String checkSql = "SELECT DISTINCT task_assignment FROM mixture_task WHERE task_id = ? OR task_id LIKE ?";
                List<String> assignments = jdbcTemplate.queryForList(checkSql, String.class, taskId, taskIdPrefix + "%");
                logger.info("数据库中存在的任务分配: {}", assignments);
            }
        } catch (Exception e) {
            logger.error("更新任务状态时出错: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取混合料任务中各实验类型的状态
     *
     * @param taskId 任务ID
     * @return 各实验类型的状态列表
     */
    public List<Map<String, String>> getExperimentTypeStatusByTaskId(String taskId) {
        List<Map<String, String>> statusList = new ArrayList<>();

        try {
            // 提取任务ID前缀（如果包含连字符）
            String taskIdPrefix = taskId;
            int dashIndex = taskId.indexOf('-');
            if (dashIndex > 0) {
                taskIdPrefix = taskId.substring(0, dashIndex);
            }

            // 查找任务记录
            List<Map<String, Object>> tasks = mixtureTaskRepository.findAllByTaskIdPrefixNative(taskId);

            if (tasks.isEmpty()) {
                logger.warn("未找到任务ID前缀为 {} 的任务记录", taskId);
                return statusList;
            }

            // 按照taskId分组处理任务
            Map<String, Map<String, String>> taskStatusMap = new HashMap<>();

            // 遍历任务获取状态
            for (Map<String, Object> task : tasks) {
                String currentTaskId = (String) task.get("task_id");
                if (currentTaskId == null) continue;

                // 获取或创建此任务ID的状态映射
                Map<String, String> statusMap = taskStatusMap.computeIfAbsent(currentTaskId, k -> new HashMap<>());

                // 设置任务ID
                statusMap.put("taskId", currentTaskId);

                // 设置状态字段
                String prepareStatus = (String) task.get("prepare_status");
                prepareStatus = (prepareStatus == null) ? "unfinished" : prepareStatus.toLowerCase();
                statusMap.put("prepareStatus", prepareStatus);

                String makingStatus = (String) task.get("making_status");
                makingStatus = (makingStatus == null) ? "unfinished" : makingStatus.toLowerCase();
                statusMap.put("makingStatus", makingStatus);

                String testingStatus = (String) task.get("testing_status");
                testingStatus = (testingStatus == null) ? "unfinished" : testingStatus.toLowerCase();
                statusMap.put("testingStatus", testingStatus);
            }

            // 将映射转换为列表
            statusList.addAll(taskStatusMap.values());

            logger.info("任务ID {} 的状态列表: {}", taskId, statusList);
            return statusList;

        } catch (Exception e) {
            logger.error("获取实验类型状态时发生错误", e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            statusList.add(errorMap);
            return statusList;
        }
    }

    /**
     * 获取任务指派信息
     *
     * @param taskId 任务ID
     * @return 任务指派信息
     */
    public String getTaskAssignment(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            logger.error("任务ID为空，无法获取任务指派信息");
            return null;
        }

        try {
            // 不使用Hibernate直接查询，而是使用JDBC参数化查询
            String sql = "SELECT task_assignment FROM mixture_task WHERE task_id = ?";
            Object[] params = new Object[]{taskId};
            int[] types = new int[]{java.sql.Types.VARCHAR}; // 明确指定参数类型为VARCHAR

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params, types);

            if (!results.isEmpty() && results.get(0).get("task_assignment") != null) {
                return (String) results.get(0).get("task_assignment");
            }

            // 如果获取不到，尝试模糊查询（针对包含通配符的任务ID）
            String taskIdPrefix = taskId.split("-")[0] + "%";
            String wildcardSql = "SELECT task_assignment FROM mixture_task WHERE task_id LIKE ?";
            Object[] wildcardParams = new Object[]{taskIdPrefix};
            int[] wildcardTypes = new int[]{java.sql.Types.VARCHAR};

            List<Map<String, Object>> wildcardResults = jdbcTemplate.queryForList(wildcardSql, wildcardParams, wildcardTypes);

            if (!wildcardResults.isEmpty() && wildcardResults.get(0).get("task_assignment") != null) {
                return (String) wildcardResults.get(0).get("task_assignment");
            }

            return null;
        } catch (Exception e) {
            logger.error("获取任务指派信息失败", e);
            return null;
        }
    }

    /**
     * 获取配比名称和压实方法
     *
     * @param taskId 任务ID
     * @return 配比和压实方法信息
     */
    public MixratioAndCompactionResponse getMixratioAndCompaction(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            logger.error("任务ID为空，无法获取配比和压实方法信息");
            return null;
        }

        try {
            // 首先从mixture_task表获取mixratio_id和specimen_id
            String sql = "SELECT mixratio_id, specimen_id FROM mixture_task WHERE task_id = ?";
            Object[] params = new Object[]{taskId};
            int[] types = new int[]{java.sql.Types.VARCHAR}; // 确保任务ID作为VARCHAR处理

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params, types);

            if (results.isEmpty()) {
                logger.warn("未找到任务 {} 的mixratio_id和specimen_id", taskId);
                return null;
            }

            // 获取mixratio_id和specimen_id并尝试转换为Long
            Long mixratioId = null;
            Long specimenId = null;

            if (results.get(0).get("mixratio_id") != null) {
                try {
                    mixratioId = Long.parseLong(results.get(0).get("mixratio_id").toString());
                } catch (NumberFormatException e) {
                    logger.warn("无法将mixratio_id转换为Long: {}", results.get(0).get("mixratio_id"));
                }
            }

            if (results.get(0).get("specimen_id") != null) {
                try {
                    specimenId = Long.parseLong(results.get(0).get("specimen_id").toString());
                } catch (NumberFormatException e) {
                    logger.warn("无法将specimen_id转换为Long: {}", results.get(0).get("specimen_id"));
                }
            }

            String mixName = null;
            String compactionMethod = null;

            // 根据mixratio_id查询mixratio表获取mix_name
            if (mixratioId != null) {
                String mixratioSql = "SELECT mix_name FROM mixratio WHERE id = ?";
                List<Map<String, Object>> mixratioResults = jdbcTemplate.queryForList(mixratioSql, mixratioId);

                if (!mixratioResults.isEmpty() && mixratioResults.get(0).get("mix_name") != null) {
                    mixName = mixratioResults.get(0).get("mix_name").toString();
                    logger.info("找到配比 {} 的名称: {}", mixratioId, mixName);
                }
            }

            // 根据specimen_id查询specimens表获取compaction_method
            if (specimenId != null) {
                String specimenSql = "SELECT compaction_method FROM specimens WHERE id = ?";
                List<Map<String, Object>> specimenResults = jdbcTemplate.queryForList(specimenSql, specimenId);

                if (!specimenResults.isEmpty() && specimenResults.get(0).get("compaction_method") != null) {
                    compactionMethod = specimenResults.get(0).get("compaction_method").toString();
                    logger.info("找到试件 {} 的压实方法: {}", specimenId, compactionMethod);
                }
            }

            return new MixratioAndCompactionResponse(mixName, compactionMethod);
        } catch (Exception e) {
            logger.error("获取配比和压实方法信息失败", e);
            return null;
        }
    }

    /**
     * 根据任务ID获取动态模量试验数据
     *
     * @param taskId 任务ID
     * @return 动态模量试验数据列表
     */
    public List<Map<String, Object>> getDynamicModulusTestByTaskId(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            logger.info("获取任务ID: {} 的动态模量试验数据", taskId);

            // 提取任务ID前缀
            String taskIdPrefix = extractTaskIdPrefix(taskId);
            logger.info("使用任务ID前缀: {} 查询动态模量试验数据", taskIdPrefix);

            // 首先获取试验ID
            String testIdSql = "SELECT id FROM dynamic_modulus_test WHERE task_id LIKE ?";
            List<Long> testIds = jdbcTemplate.queryForList(testIdSql, Long.class, taskIdPrefix + "%");

            if (testIds.isEmpty()) {
                logger.warn("未找到任务ID: {} 的动态模量试验数据", taskId);
                return result;
            }

            for (Long testId : testIds) {
                Map<String, Object> testData = new HashMap<>();

                // 1. 获取试验基本信息
                String testSql = "SELECT id as test_id, task_id, mix_ratio_id, mix_ratio_name, mix_ratio_display_name " +
                        "FROM dynamic_modulus_test WHERE id = ?";
                Map<String, Object> testInfo = jdbcTemplate.queryForMap(testSql, testId);
                testData.putAll(testInfo);

                // 2. 获取试件信息
                String specimenSql = "SELECT specimen_number, diameter, height, bulk_density, air_void_content " +
                        "FROM dynamic_modulus_specimen WHERE test_id = ? LIMIT 1";
                List<Map<String, Object>> specimens = jdbcTemplate.queryForList(specimenSql, testId);

                if (!specimens.isEmpty()) {
                    testData.put("specimen", specimens.get(0));
                }

                // 3. 获取温度信息和测量数据，按温度分组
                String tempSql = "SELECT id, temperature, temperature_order FROM dynamic_modulus_temperature " +
                        "WHERE test_id = ? ORDER BY temperature_order";
                List<Map<String, Object>> temperatures = jdbcTemplate.queryForList(tempSql, testId);

                List<Map<String, Object>> temperatureGroups = new ArrayList<>();

                for (Map<String, Object> temp : temperatures) {
                    Long temperatureId = (Long) temp.get("id");
                    Map<String, Object> tempGroup = new HashMap<>();
                    tempGroup.put("temperature", temp.get("temperature"));

                    // 4. 获取该温度下的所有测量数据
                    String measurementSql = "SELECT frequency, cycle_count, dynamic_modulus, phase_angle, " +
                            "axial_stress, axial_strain, permanent_deformation as permanent_strain " +
                            "FROM dynamic_modulus_measurement " +
                            "WHERE test_id = ? AND temperature_id = ? " +
                            "ORDER BY frequency DESC";
                    List<Map<String, Object>> measurements = jdbcTemplate.queryForList(measurementSql, testId, temperatureId);

                    // 记录测量数据的结构和内容
                    logger.info("温度ID: {}, 温度: {}°C, 测量数据条数: {}",
                            temperatureId, temp.get("temperature"), measurements.size());
                    if (!measurements.isEmpty()) {
                        logger.info("第一条测量数据示例: {}", measurements.get(0));
                    }

                    tempGroup.put("measurements", measurements);
                    temperatureGroups.add(tempGroup);
                }

                testData.put("temperatureGroups", temperatureGroups);
                result.add(testData);
            }

            logger.info("成功获取任务ID: {} 的动态模量试验数据，共 {} 条记录", taskId, result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取动态模量试验数据时出错: {}", e.getMessage(), e);
            return result;
        }
    }

    /**
     * 根据任务ID获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据
     *
     * @param taskId 任务ID
     * @return 沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据列表
     */
    public List<Map<String, Object>> getDirectStretchingFatigueTestByTaskId(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 提取任务ID前缀，去掉末尾的数字后缀
            String taskIdPrefix = extractTaskIdPrefix(taskId);
            logger.info("使用任务ID前缀: {} 查询沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据", taskIdPrefix);

            // 首先获取试验ID
            String testIdSql = "SELECT id FROM direct_stretching_fatigue_test WHERE task_id LIKE ?";
            List<Long> testIds = jdbcTemplate.queryForList(testIdSql, Long.class, taskIdPrefix + "%");

            if (testIds.isEmpty()) {
                logger.warn("未找到任务ID: {} 的沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据", taskId);
                return result;
            }

            for (Long testId : testIds) {
                Map<String, Object> testData = new HashMap<>();

                // 1. 获取试验基本信息
                String testSql = "SELECT t.id as test_id, t.task_id, t.mix_ratio_id " +
                        "FROM direct_stretching_fatigue_test t " +
                        "LEFT JOIN mixratio m ON t.mix_ratio_id = CAST(m.id AS VARCHAR) " +
                        "WHERE t.id = ?";
                Map<String, Object> testInfo = jdbcTemplate.queryForMap(testSql, testId);
                testData.putAll(testInfo);

                // 2. 获取试件信息
                String specimenSql = "SELECT id as specimen_db_id, specimen_id, height, diameter " +
                        "FROM direct_stretching_fatigue_specimens WHERE test_id = ? LIMIT 1";
                List<Map<String, Object>> specimens = jdbcTemplate.queryForList(specimenSql, testId);

                List<Map<String, Object>> specimenDataList = new ArrayList<>();

                // 3. 获取每个试件的动态模量和疲劳数据
                for (Map<String, Object> specimen : specimens) {
                    Map<String, Object> specimenData = new HashMap<>();
                    Long specimenDbId = (Long) specimen.get("specimen_db_id");

                    // 4. 获取动态模量数据
                    String modulusSql = "SELECT stage, dynamic_modulus, cycle_count, phase_angle, force_level, " +
                            "equilibrium_strain, temperature " +
                            "FROM direct_stretching_modulus_data " +
                            "WHERE specimen_id = ? " +
                            "ORDER BY stage";
                    List<Map<String, Object>> modulusDataList = jdbcTemplate.queryForList(modulusSql, specimenDbId);

                    specimenData.put("modulus_data", modulusDataList);

                    // 5. 获取疲劳数据
                    String fatigueSql = "SELECT stage, cycle_count, phase_angle, force_level, " +
                            "equilibrium_strain, temperature " +
                            "FROM direct_stretching_fatigue_data " +
                            "WHERE specimen_id = ? " +
                            "ORDER BY stage";
                    List<Map<String, Object>> fatigueDataList = jdbcTemplate.queryForList(fatigueSql, specimenDbId);

                    specimenData.put("fatigue_data", fatigueDataList);

                    specimenDataList.add(specimenData);
                }

                testData.put("specimens", specimenDataList);
                result.add(testData);
            }

            logger.info("成功获取任务ID: {} 的沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据，共 {} 条记录", taskId, result.size());
            return result;
        } catch (Exception e) {
            logger.error("获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据时出错: {}", e.getMessage(), e);
            throw new RuntimeException("获取沥青混合料直接拉伸循环疲劳测黏弹损伤实验数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取沥青混合料四点弯曲疲劳寿命试验数据
     *
     * @param taskId 任务ID
     * @return 包含试验数据的Map
     */
    public Map<String, Object> getFourPointBendingTestData(String taskId) {
        logger.info("获取沥青混合料四点弯曲疲劳寿命试验数据，任务ID: {}", taskId);

        try {
            // 查询测试基本信息
            String testSql = "SELECT * FROM mixture_four_point_bending_test WHERE task_id LIKE ? ORDER BY created_at DESC LIMIT 1";
            List<Map<String, Object>> testResults = jdbcTemplate.queryForList(testSql, taskId + "%");

            if (testResults == null || testResults.isEmpty()) {
                logger.warn("未找到沥青混合料四点弯曲疲劳寿命试验数据，任务ID: {}", taskId);
                return Collections.emptyMap();
            }

            Map<String, Object> testData = testResults.get(0);
            Long testId = ((Number) testData.get("id")).longValue();

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("task_id", taskId);
            result.put("test_date", testData.get("test_date"));
            result.put("operator", testData.get("operator"));

            // 查询试件数据
            String specimenSql = "SELECT * FROM mixture_four_point_bending_specimen WHERE test_id = ? ORDER BY specimen_number";
            List<Map<String, Object>> specimenList = jdbcTemplate.queryForList(specimenSql, testId);

            // 转换为前端期望的格式
            List<Map<String, Object>> specimens = new ArrayList<>();
            for (Map<String, Object> specimen : specimenList) {
                Map<String, Object> specimenData = new HashMap<>();
                Long specimenId = ((Number) specimen.get("id")).longValue();

                // 基本字段
                specimenData.put("specimen_number", specimen.get("specimen_number"));
                specimenData.put("length_mm", specimen.get("length_mm"));
                specimenData.put("width_mm", specimen.get("width_mm"));
                specimenData.put("height_mm", specimen.get("height_mm"));
                specimenData.put("span_mm", specimen.get("span_mm"));
                specimenData.put("strain_range", specimen.get("strain_range"));
                specimenData.put("frequency_hz", specimen.get("frequency_hz"));
                specimenData.put("test_temperature", specimen.get("test_temperature"));
                specimenData.put("fatigue_life", specimen.get("fatigue_life"));

                // 查询结果数据
                String resultSql = "SELECT * FROM mixture_four_point_bending_result WHERE specimen_id = ? ORDER BY result_index";
                List<Map<String, Object>> resultsList = jdbcTemplate.queryForList(resultSql, specimenId);

                if (!resultsList.isEmpty()) {
                    List<Map<String, Object>> results = new ArrayList<>();
                    for (Map<String, Object> resultItem : resultsList) {
                        Map<String, Object> resultData = new HashMap<>();
                        resultData.put("result_type", resultItem.get("result_type"));
                        resultData.put("result_type_display_name", resultItem.get("result_type_display_name"));
                        resultData.put("result_type_english_name", resultItem.get("result_type_english_name"));
                        resultData.put("result_type_unit", resultItem.get("result_type_unit"));
                        resultData.put("result_index", resultItem.get("result_index"));
                        resultData.put("initial_value", resultItem.get("initial_value"));
                        resultData.put("current_value", resultItem.get("current_value"));
                        results.add(resultData);
                    }
                    specimenData.put("results", results);
                }

                specimens.add(specimenData);
            }

            result.put("specimens", specimens);

            logger.info("成功获取沥青混合料四点弯曲疲劳寿命试验数据，任务ID: {}, 试件数量: {}", taskId, specimens.size());
            return result;
        } catch (Exception e) {
            logger.error("获取沥青混合料四点弯曲疲劳寿命试验数据时出错: {}", e.getMessage(), e);
            throw new RuntimeException("获取沥青混合料四点弯曲疲劳寿命试验数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取沥青混合料单轴压缩试验（圆柱体法）数据
     *
     * @param taskId 任务ID
     * @return 包含试验数据的Map
     */
    public Map<String, Object> getUniaxialCompressionTestData(String taskId) {
        logger.info("获取沥青混合料单轴压缩试验（圆柱体法）数据，任务ID: {}", taskId);

        try {
            Map<String, Object> result = new HashMap<>();

            // 提取任务ID前缀，去掉末尾的数字后缀
            String taskIdPrefix = extractTaskIdPrefix(taskId);
            logger.info("使用任务ID前缀: {} 查询单轴压缩试验数据", taskIdPrefix);

            // 1. 查询测试基本信息
            String testSql = "SELECT * FROM mixture_uniaxial_compression_test WHERE task_id LIKE ? ORDER BY created_at DESC LIMIT 1";
            List<Map<String, Object>> testResults = jdbcTemplate.queryForList(testSql, taskIdPrefix + "%");


            if (testResults == null || testResults.isEmpty()) {
                logger.warn("未找到沥青混合料单轴压缩试验（圆柱体法）数据，任务ID: {}", taskId);
                return Collections.emptyMap();
            }

            Map<String, Object> testData = testResults.get(0);
            String testId = (String) testData.get("test_id");

            // 设置基本信息
            result.put("taskId", taskId);

            // 处理日期字段 - 转换为前端期望的格式
            Object testDateObj = testData.get("test_date");
            if (testDateObj != null) {
                result.put("testDate", testDateObj);
            }

            // 处理温度字段 - 确保前端拿到数值类型
            Object tempObj = testData.get("test_temperature");
            if (tempObj != null) {
                Double testTemp = parseDoubleValue(tempObj);
                result.put("testTemperature", testTemp);
            }

            // 2. 查询试件数据
            String specimenSql = "SELECT * FROM mixture_uniaxial_compression_specimen WHERE test_id = ? ORDER BY specimen_number";
            List<Map<String, Object>> specimenList = jdbcTemplate.queryForList(specimenSql, testId);

            // 3. 处理试件数据
            List<Map<String, Object>> specimens = new ArrayList<>();
            for (Map<String, Object> specimen : specimenList) {
                Map<String, Object> specimenData = new HashMap<>();
                String specimenId = (String) specimen.get("specimen_id");

                // 设置试件基本数据 - 确保数值字段为数值类型
                specimenData.put("specimenNumber", parseIntValue(specimen.get("specimen_number")));
                specimenData.put("diameterMm", parseDoubleValue(specimen.get("diameter")));
                specimenData.put("heightMm", parseDoubleValue(specimen.get("height")));

                // 4. 查询UTM数据 - 确保包含所有前端需要的字段
                Map<String, Object> utmData = retrieveUtmData(specimenId);
                if (!utmData.isEmpty()) {
                    // 将UTM数据列表直接添加到specimenData
                    specimenData.put("utmData", utmData);
                    // 记录日志，跟踪数据
                    if (utmData.containsKey("utmDataList")) {
                        List<?> utmList = (List<?>)utmData.get("utmDataList");
                        logger.info("试件ID: {} 的UTM数据列表大小: {}", specimenId, utmList.size());
                        if (!utmList.isEmpty()) {
                            logger.info("第一条UTM数据: pressureLevel={}, maxForce={}, minForce={}", 
                                ((Map<?,?>)utmList.get(0)).get("pressureLevel"),
                                ((Map<?,?>)utmList.get(0)).get("maxForceKn"),
                                ((Map<?,?>)utmList.get(0)).get("minForceN"));
                        }
                    }
                }

                // 5. 查询P值数据并计算平均值
                List<Map<String, Object>> strengthData = retrieveStrengthData(specimenId);
                if (!strengthData.isEmpty()) {
                    specimenData.put("strengthData", strengthData);

                    // 计算并添加强度平均值
                    double strengthAvg = calculateStrengthAverage(strengthData);
                    specimenData.put("strengthAverage", strengthAvg);
                }

                specimens.add(specimenData);
            }

            result.put("specimens", specimens);
            return result;

        } catch (Exception e) {
            logger.error("获取沥青混合料单轴压缩试验（圆柱体法）数据时出错: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * 获取试件的UTM数据
     */
    private Map<String, Object> retrieveUtmData(String specimenId) {
        Map<String, Object> utmMap = new HashMap<>();

        String utmSql = "SELECT * FROM mixture_uniaxial_compression_uts028_data WHERE specimen_id = ? ORDER BY pressure_level";
        List<Map<String, Object>> utmResults = jdbcTemplate.queryForList(utmSql, specimenId);

        if (utmResults.isEmpty()) {
            logger.warn("未找到试件ID: {} 的UTM数据", specimenId);
            return utmMap;
        }

        // 始终创建UTM数据列表
        List<Map<String, Object>> utmDataList = new ArrayList<>();

        // 检查是否有多个压力级别
        if (utmResults.size() > 1) {
            logger.info("试件ID: {} 有多个压力级别的UTM数据，共 {} 个", specimenId, utmResults.size());

            // 处理多个压力级别
            for (Map<String, Object> utmData : utmResults) {
                Map<String, Object> utmItem = new HashMap<>();

                // 确保包含压力级别
                String pressureLevel = (String) utmData.get("pressure_level");
                utmItem.put("pressureLevel", pressureLevel);

                // 使用与前端一致的字段名称格式，保持snake_case
                utmItem.put("max_force", parseDoubleValue(utmData.get("max_force")));
                utmItem.put("min_force", parseDoubleValue(utmData.get("min_force")));
                utmItem.put("work_ratio", parseDoubleValue(utmData.get("work_ratio")));
                utmItem.put("displacement", parseDoubleValue(utmData.get("displacement")));
                utmItem.put("strain", parseDoubleValue(utmData.get("strain")));
                utmItem.put("rebound_modulus", parseDoubleValue(utmData.get("rebound_modulus")));
                utmItem.put("temperature", parseDoubleValue(utmData.get("temperature")));

                // 同时保留驼峰命名的字段，以保持向后兼容性
                utmItem.put("maxForceKn", parseDoubleValue(utmData.get("max_force")));
                utmItem.put("minForceN", parseDoubleValue(utmData.get("min_force")));
                utmItem.put("stressDevKpa", parseDoubleValue(utmData.get("work_ratio")));
                utmItem.put("displResilMm", parseDoubleValue(utmData.get("displacement")));
                utmItem.put("strainResil", parseDoubleValue(utmData.get("strain")));
                utmItem.put("resilientModulusMpa", parseDoubleValue(utmData.get("rebound_modulus")));

                utmDataList.add(utmItem);
            }
        } else {
            // 单个压力级别也创建列表
            Map<String, Object> utmData = utmResults.get(0);
            logger.info("试件ID: {} 只有一个压力级别的UTM数据: {}", specimenId, utmData.get("pressure_level"));

            Map<String, Object> utmItem = new HashMap<>();
            utmItem.put("pressureLevel", utmData.get("pressure_level"));
            
            // 使用与前端一致的字段名称格式，保持snake_case
            utmItem.put("max_force", parseDoubleValue(utmData.get("max_force")));
            utmItem.put("min_force", parseDoubleValue(utmData.get("min_force")));
            utmItem.put("work_ratio", parseDoubleValue(utmData.get("work_ratio")));
            utmItem.put("displacement", parseDoubleValue(utmData.get("displacement")));
            utmItem.put("strain", parseDoubleValue(utmData.get("strain")));
            utmItem.put("rebound_modulus", parseDoubleValue(utmData.get("rebound_modulus")));
            utmItem.put("temperature", parseDoubleValue(utmData.get("temperature")));

            // 同时保留驼峰命名的字段，以保持向后兼容性
            utmItem.put("maxForceKn", parseDoubleValue(utmData.get("max_force")));
            utmItem.put("minForceN", parseDoubleValue(utmData.get("min_force")));
            utmItem.put("stressDevKpa", parseDoubleValue(utmData.get("work_ratio")));
            utmItem.put("displResilMm", parseDoubleValue(utmData.get("displacement")));
            utmItem.put("strainResil", parseDoubleValue(utmData.get("strain")));
            utmItem.put("resilientModulusMpa", parseDoubleValue(utmData.get("rebound_modulus")));

            utmDataList.add(utmItem);
        }

        // 直接将utmDataList放入返回结果
        utmMap.put("utmDataList", utmDataList);
        return utmMap;
    }

    /**
     * 获取试件的强度数据(P值)
     */
    private List<Map<String, Object>> retrieveStrengthData(String specimenId) {
        List<Map<String, Object>> strengthDataList = new ArrayList<>();

        String pValuesSql = "SELECT * FROM mixture_uniaxial_compression_p_values WHERE specimen_id = ? ORDER BY p_index";
        List<Map<String, Object>> pValuesResults = jdbcTemplate.queryForList(pValuesSql, specimenId);

        for (Map<String, Object> pValue : pValuesResults) {
            Map<String, Object> strengthData = new HashMap<>();

            // 确保索引是整数类型
            int pIndex = parseIntValue(pValue.get("p_index"));
            // 确保P值是数值类型
            double pValueDouble = parseDoubleValue(pValue.get("p_value"));

            strengthData.put("label", "P" + pIndex);
            strengthData.put("valueKn", pValueDouble);

            strengthDataList.add(strengthData);
        }

        return strengthDataList;
    }

    /**
     * 计算强度数据平均值
     */
    private double calculateStrengthAverage(List<Map<String, Object>> strengthDataList) {
        if (strengthDataList.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        int count = 0;

        for (Map<String, Object> strengthData : strengthDataList) {
            Double value = (Double) strengthData.get("valueKn");
            if (value != null) {
                sum += value;
                count++;
            }
        }

        return count > 0 ? sum / count : 0.0;
    }

    /**
     * 将对象解析为Double值
     */
    private Double parseDoubleValue(Object obj) {
        if (obj == null) {
            return 0.0;
        }

        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

        return 0.0;
    }

    /**
     * 将对象解析为Integer值
     */
    private Integer parseIntValue(Object obj) {
        if (obj == null) {
            return 0;
        }

        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        } else if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        return 0;
    }

    /**
     * 将下划线命名转换为驼峰命名
     * 例如: "user_name" -> "userName"
     */
    private String camelCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        StringBuilder result = new StringBuilder();
        boolean underscoreFound = false;

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            if (currentChar == '_') {
                underscoreFound = true;
            } else {
                if (underscoreFound) {
                    result.append(Character.toUpperCase(currentChar));
                    underscoreFound = false;
                } else {
                    result.append(currentChar);
                }
            }
        }

        return result.toString();
    }

    /**
     * 获取沥青混合料劈裂试验数据
     *
     * @param taskId 任务ID
     * @return 劈裂试验数据列表
     */
    public List<Map<String, Object>> getSplittingTestByTaskId(String taskId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 提取任务ID前缀，去掉末尾的数字后缀
            String taskIdPrefix = extractTaskIdPrefix(taskId);
            logger.info("使用任务ID前缀: {} 查询劈裂试验数据", taskIdPrefix);

            // 构建查询SQL，将主表与试件表关联
            String sql = "SELECT st.*, ss.* " +
                    "FROM mixture_splitting_test st " +
                    "LEFT JOIN mixture_splitting_test_specimen ss ON st.test_id = ss.test_id " +
                    "WHERE st.task_id LIKE ? " +
                    "ORDER BY st.test_id, ss.specimen_number";

            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, taskIdPrefix + "%");

            // 其余部分保持不变
            if (rows.isEmpty()) {
                logger.warn("未找到任务ID: {} 的沥青混合料劈裂试验数据", taskId);
                return result;
            }

            Map<String, Map<String, Object>> testMap = new HashMap<>();
            Map<String, List<Map<String, Object>>> specimenMap = new HashMap<>();

            // 遍历结果，分离测试和试件数据
            for (Map<String, Object> row : rows) {
                String testId = (String) row.get("test_id");

                // 收集测试数据
                if (!testMap.containsKey(testId)) {
                    Map<String, Object> testData = new HashMap<>();
                    testData.put("test_id", testId);
                    testData.put("task_id", row.get("task_id"));
                    testData.put("mix_ratio_id", row.get("mix_ratio_id"));
                    testData.put("test_temperature", row.get("test_temperature"));
                    testData.put("test_time", row.get("test_time"));
                    testData.put("operator", row.get("operator"));
                    testData.put("test_equipment", row.get("test_equipment"));
                    testData.put("test_method", row.get("test_method"));
                    testData.put("test_standard", row.get("test_standard"));
                    testData.put("remarks", row.get("remarks"));

                    testMap.put(testId, testData);
                    specimenMap.put(testId, new ArrayList<>());
                }

                // 收集试件数据
                if (row.get("specimen_id") != null) {
                    Map<String, Object> specimenData = new HashMap<>();
                    Long specimenId = (Long) row.get("specimen_id");

                    // 基本信息
                    specimenData.put("specimen_id", row.get("specimen_id"));
                    specimenData.put("specimen_number", row.get("specimen_number"));
                    specimenData.put("diameter", row.get("diameter"));
                    specimenData.put("height", row.get("height"));
                    specimenData.put("p1_value", row.get("p1_value"));
                    specimenData.put("p2_value", row.get("p2_value"));
                    specimenData.put("p3_value", row.get("p3_value"));
                    specimenData.put("p_average", row.get("p_average"));
                    specimenData.put("x1_value", row.get("x1_value"));
                    specimenData.put("x2_value", row.get("x2_value"));
                    specimenData.put("x3_value", row.get("x3_value"));
                    specimenData.put("x_average", row.get("x_average"));
                    specimenData.put("poisson_ratio", row.get("poisson_ratio"));
                    specimenData.put("tensile_strength", row.get("tensile_strength"));
                    specimenData.put("failure_strain", row.get("failure_strain"));
                    specimenData.put("stiffness_modulus", row.get("stiffness_modulus"));

                    specimenMap.get(testId).add(specimenData);
                }
            }

            // 组合测试和试件数据
            for (String testId : testMap.keySet()) {
                Map<String, Object> testData = testMap.get(testId);
                testData.put("specimens", specimenMap.get(testId));
                result.add(testData);
            }

            logger.info("成功获取任务ID={}的沥青混合料劈裂试验数据，共{}条", taskId, result.size());
            return result;

        } catch (Exception e) {
            logger.error("获取沥青混合料劈裂试验数据失败: {}", e.getMessage(), e);
            return result;
        }
    }
}
