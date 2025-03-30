package com.example.labdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 沥青已完成任务服务类
 */
@Service
public class AsphaltCompletedTaskService {

    private static final Logger logger = LoggerFactory.getLogger(AsphaltCompletedTaskService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取已完成的沥青任务
     * 
     * @param companyId 公司ID
     * @return 已完成的沥青任务列表
     */
    public List<Map<String, Object>> getCompletedAsphaltTasks(String companyId) {
        logger.info("正在查询公司ID为 {} 的已完成沥青任务", companyId);
        
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "    at.asphalt_task_assignment_id AS asphalt_task_id, " +
            "    at.asphalt_experiment_name AS asphalt_task_name, " +
            "    at.task_status, " +
            "    at.acceptor as username, " +  
            "    u.name as acceptor, " +       
            "    at.accept_time, " +
            "    at.created_at, " +
            "    at.updated_at, " +
            "    at.asphalt_task_assignment, " +
            "    at.asphalt_experiment_type, " +
            "    at.experiment_status, " +
            "    at.assigned_asphalt_equipment, " +
            "    at.assigned_asphalt_equipment_manufacturer " +
            "FROM " +
            "    asphalt_task at " +
            "LEFT JOIN users u ON at.acceptor = u.username " +  
            "WHERE " +
            "    at.experiment_status = 'finished' " +
            "    AND at.company_id = ? " +
            "ORDER BY " +
            "    at.created_at DESC"
        );
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), companyId);
        logger.info("查询到公司ID为 {} 的已完成沥青任务 {} 条", companyId, results.size());
        
        return results;
    }
}
