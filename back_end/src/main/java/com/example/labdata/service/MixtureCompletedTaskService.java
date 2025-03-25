package com.example.labdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

/**
 * 混合料已完成任务服务类
 */
@Service
public class MixtureCompletedTaskService {

    private static final Logger logger = LoggerFactory.getLogger(MixtureCompletedTaskService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取已完成的沥青任务
     * 
     * @param companyId 公司ID
     * @return 已完成的沥青任务列表
     */
    public List<Map<String, Object>> getCompletedAsphaltTasks(String companyId) {
        logger.info("正在查询公司ID为 {} 的已完成混合料任务", companyId);
        
        StringBuilder sql = new StringBuilder(
            "SELECT " +
            "    task_id AS task_id, " +
            "    task_name AS task_name, " +
            "    status, " +
            "    acceptor, " +
            "    accept_time, " +
            "    creation_time, " +
            "    task_assignment, " +
            "FROM " +
            "    mixture_task " +
            "WHERE " +
            "    status = 'COMPLETED' " +
            "    AND company_id = ? " +
            "ORDER BY " +
            "    creation_time DESC"
        );
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql.toString(), companyId);
        logger.info("查询到公司ID为 {} 的已完成混合料任务 {} 条", companyId, results.size());
        
        return results;
    }
    
    /**
     * 获取已完成的混合料任务
     * 
     * @param companyId 公司ID
     * @return 已完成的混合料任务列表
     */
    public List<Map<String, Object>> getCompletedMixtureTasks(String companyId) {
        logger.info("正在查询公司ID为 {} 的已完成混合料任务", companyId);
        
        // 确保companyId是字符串类型
        String companyIdStr = String.valueOf(companyId);
        
        // 使用显式类型转换避免类型不匹配问题
        String sql = 
            "SELECT " +
            "    mt.task_id, " +
            "    mt.task_name, " +
            "    mt.status, " +
            "    mt.acceptor, " +
            "    mt.accept_time, " +
            "    mt.creation_time, " +
            "    mt.task_assignment, " +
            "    mt.task_company " +
            "FROM " +
            "    mixture_task mt " +
            "WHERE " +
            "    mt.status = 'COMPLETE' " +
            "    AND CAST(mt.task_company AS VARCHAR) = CAST(? AS VARCHAR) " + // 使用CAST函数显式转换
            "ORDER BY " +
            "    mt.creation_time DESC";
        
        // 使用参数类型提示确保参数被视为VARCHAR
        List<Map<String, Object>> results = jdbcTemplate.query(
            sql,
            new Object[]{companyIdStr},
            new int[]{java.sql.Types.VARCHAR},
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                ResultSetMetaData metaData;
                try {
                    metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                } catch (SQLException e) {
                    logger.error("读取结果集元数据失败", e);
                }
                return row;
            }
        );
        
        logger.info("查询到公司ID为 {} 的已完成混合料任务 {} 条", companyId, results.size());
        return results;
    }
}