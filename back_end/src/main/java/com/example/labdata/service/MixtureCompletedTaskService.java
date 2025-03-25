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
            "    mt.mixratio_id, " +
            "    mt.specimen_id, " +
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


        // 为每个任务查询配比名称和压实方法
        for (Map<String, Object> task : results) {
            // 获取mixratio_id用于查询配比名称
            Object mixratioId = task.get("mixratio_id");
            logger.debug("任务ID: {}, mixratio_id: {}", task.get("task_id"), mixratioId);
            
            if (mixratioId != null) {
                try {
                    String mixQuerySql = "SELECT mix_name FROM mixratio WHERE id::text = ?";
                    List<Map<String, Object>> mixResults = jdbcTemplate.queryForList(mixQuerySql, String.valueOf(mixratioId));
                    if (!mixResults.isEmpty() && mixResults.get(0).containsKey("mix_name")) {
                        Object mixName = mixResults.get(0).get("mix_name");
                        task.put("mix_name", mixName);
                        logger.debug("为任务ID: {} 添加配比名称: {}", task.get("task_id"), mixName);
                    } else {
                        logger.warn("未找到mixratio_id: {} 对应的配比名称", mixratioId);
                    }
                } catch (Exception e) {
                    logger.warn("查询配比名称失败: {}", e.getMessage());
                }
            }
            
            // 获取specimen_id用于查询压实方法
            Object specimenId = task.get("specimen_id");
            logger.debug("任务ID: {}, specimen_id: {}", task.get("task_id"), specimenId);
            
            if (specimenId != null) {
                try {
                    String specimenQuerySql = "SELECT compaction_method FROM specimens WHERE id::text = ?";
                    List<Map<String, Object>> specimenResults = jdbcTemplate.queryForList(specimenQuerySql, String.valueOf(specimenId));
                    if (!specimenResults.isEmpty() && specimenResults.get(0).containsKey("compaction_method")) {
                        Object compactionMethod = specimenResults.get(0).get("compaction_method");
                        task.put("compaction_method", compactionMethod);
                        logger.debug("为任务ID: {} 添加压实方法: {}", task.get("task_id"), compactionMethod);
                    } else {
                        logger.warn("未找到specimen_id: {} 对应的压实方法", specimenId);
                    }
                } catch (Exception e) {
                    logger.warn("查询压实方法失败: {}", e.getMessage());
                }
            }
        }
        
        // 打印完整的处理后结果
        logger.info("已为 {} 条记录添加配比名称和压实方法信息", results.size());
        for (Map<String, Object> task : results) {
            logger.debug("处理后的任务数据: task_id={}, mix_name={}, compaction_method={}", 
                task.get("task_id"), task.get("mix_name"), task.get("compaction_method"));
        }

        return results;
    }
}