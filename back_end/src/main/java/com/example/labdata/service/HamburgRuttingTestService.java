package com.example.labdata.service;

import com.example.labdata.model.HamburgRuttingTest;
import com.example.labdata.repository.HamburgRuttingTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 汉堡车辙实验数据服务
 */
@Service
public class HamburgRuttingTestService {

    private final HamburgRuttingTestRepository hamburgRuttingTestRepository;
    private static final Logger logger = LoggerFactory.getLogger(HamburgRuttingTestService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public HamburgRuttingTestService(HamburgRuttingTestRepository hamburgRuttingTestRepository,
                                     JdbcTemplate jdbcTemplate) {
        this.hamburgRuttingTestRepository = hamburgRuttingTestRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存汉堡车辙实验数据
     *
     * @param hamburgRuttingTest 要保存的数据
     * @return 已保存的数据
     */
    public HamburgRuttingTest saveHamburgRuttingTest(HamburgRuttingTest hamburgRuttingTest) {
        // 检查是否已存在该任务和配比的数据
        Optional<HamburgRuttingTest> existingData = hamburgRuttingTestRepository
            .findByTaskIdAndMixRatioId(hamburgRuttingTest.getTaskId(), hamburgRuttingTest.getMixRatioId());
        
        // 如果存在，则更新数据而不是创建新记录
        if (existingData.isPresent()) {
            HamburgRuttingTest existing = existingData.get();
            existing.setSteadySlope1(hamburgRuttingTest.getSteadySlope1());
            existing.setSteadyCurvilinear1(hamburgRuttingTest.getSteadyCurvilinear1());
            existing.setSteadySlope2(hamburgRuttingTest.getSteadySlope2());
            existing.setSteadyCurvilinear2(hamburgRuttingTest.getSteadyCurvilinear2());
            return hamburgRuttingTestRepository.save(existing);
        }
        

        // 更新任务状态
        String taskAssignment = "沥青混合料车辙实验（汉堡车辙）";
        updateExperimentTaskStatus(hamburgRuttingTest.getTaskId(), taskAssignment);
        // 否则创建新记录
        return hamburgRuttingTestRepository.save(hamburgRuttingTest);
    }

    /**
     * 根据任务ID获取汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据列表
     */
    public List<HamburgRuttingTest> getHamburgRuttingTestsByTaskId(String taskId) {
        
        

        return hamburgRuttingTestRepository.findByTaskId(taskId);
    }

    /**
     * 根据任务ID和配比ID获取汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 汉堡车辙实验数据，如果不存在则返回null
     */
    public HamburgRuttingTest getHamburgRuttingTestByTaskIdAndMixRatioId(String taskId, Long mixRatioId) {
        return hamburgRuttingTestRepository.findByTaskIdAndMixRatioId(taskId, mixRatioId).orElse(null);
    }
    /**
     * 更新实验任务状态
     * @param taskId 任务ID
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
            String sql = "UPDATE mixture_task SET testing_status = 'finished' WHERE task_id = ? AND task_assignment = ?";
             // 使用taskId进行精确匹配
        int updatedRows = jdbcTemplate.update(sql, taskId, taskAssignment);
        
        if (updatedRows == 0) {
            logger.warn("未找到匹配的任务(精确匹配)，尝试使用前缀匹配");
            // 如果精确匹配未成功，尝试使用前缀匹配
            sql = "UPDATE mixture_task SET testing_status = 'finished' WHERE task_id LIKE ? AND task_assignment = ?";
            updatedRows = jdbcTemplate.update(sql, taskIdPrefix + "%", taskAssignment);
        }
        
        if (updatedRows > 0) {
            logger.info("成功更新任务ID: {} 的实验类型: {} 的状态为finished，影响行数: {}", taskId, taskAssignment, updatedRows);
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
}
