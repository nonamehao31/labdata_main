package com.example.labdata.service;

import com.example.labdata.model.MarshallTest;
import com.example.labdata.payload.request.MarshallTestRequest;
import com.example.labdata.repository.MarshallTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

/**
 * 马歇尔试验数据服务
 */
@Service
public class MarshallTestService {

    private static final Logger logger = LoggerFactory.getLogger(MarshallTestService.class);

    private final MarshallTestRepository marshallTestRepository;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MarshallTestService(MarshallTestRepository marshallTestRepository, JdbcTemplate jdbcTemplate) {
        this.marshallTestRepository = marshallTestRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 保存马歇尔试验数据
     *
     * @param request 马歇尔试验数据请求
     * @return 保存的马歇尔试验数据
     */
    public MarshallTest saveMarshallTest(MarshallTestRequest request) {
        logger.info("保存马歇尔试验数据，任务ID: {}", request.getTaskId());
        
        // 查找是否已经存在该任务的马歇尔实验数据
        List<MarshallTest> existingTests = marshallTestRepository.findByTaskId(request.getTaskId());
        
        MarshallTest test;
        if (!existingTests.isEmpty()) {
            // 更新现有记录
            test = existingTests.get(0);
            logger.info("更新现有马歇尔试验数据，ID: {}", test.getId());
        } else {
            // 创建新记录
            test = new MarshallTest();
            test.setTaskId(request.getTaskId());
            logger.info("创建新的马歇尔试验数据记录");
        }
        
        // 设置测试数据
        test.setStability1(request.getStability1());
        test.setStreamValue1(request.getStreamValue1());
        test.setStability2(request.getStability2());
        test.setStreamValue2(request.getStreamValue2());
        test.setStability3(request.getStability3());
        test.setStreamValue3(request.getStreamValue3());
        
        
        // 更新任务状态
        String taskAssignment = "马歇尔稳定度试验";
        updateExperimentTaskStatus(request.getTaskId(), taskAssignment);
        // 保存到数据库
        return marshallTestRepository.save(test);
    }
    
    /**
     * 根据任务ID获取马歇尔试验数据
     *
     * @param taskId 任务ID
     * @return 马歇尔试验数据列表
     */
    public List<MarshallTest> getMarshallTestsByTaskId(String taskId) {
        logger.info("获取任务ID: {} 的马歇尔试验数据", taskId);
        return marshallTestRepository.findAllByTaskIdNative(taskId);
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
