package com.example.labdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 混合料任务状态更新服务
 */
@Service
public class MixtureTaskStatusService {

    private static final Logger logger = LoggerFactory.getLogger(MixtureTaskStatusService.class);
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MixtureTaskStatusService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 更新实验完成状态
     * 将任务的testing_status设为finished，status设为COMPLETE
     *
     * @param taskId 任务ID
     * @param taskAssignment 实验类型
     * @return 更新结果，true表示成功，false表示失败
     */
    public boolean updateExperimentCompleteStatus(String taskId, String taskAssignment) {
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
                return true;
            } else {
                logger.warn("没有找到匹配的任务记录: task_id={}, task_assignment={}", taskId, taskAssignment);
                // 输出可能的任务分配值，以便调试
                String checkSql = "SELECT DISTINCT task_assignment FROM mixture_task WHERE task_id = ? OR task_id LIKE ?";
                List<String> assignments = jdbcTemplate.queryForList(checkSql, String.class, taskId, taskIdPrefix + "%");
                logger.info("数据库中存在的任务分配: {}", assignments);
                return false;
            }
        } catch (Exception e) {
            logger.error("更新任务状态时出错: {}", e.getMessage(), e);
            return false;
        }
    }
}
