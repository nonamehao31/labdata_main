package com.example.labdata.repository;

import com.example.labdata.model.PenetrationTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 针入度试验数据仓库
 * 注意：使用String类型处理taskId，避免大数值问题
 */
@Repository
public interface PenetrationTestRepository extends JpaRepository<PenetrationTest, Long> {
    
    /**
     * 根据任务ID查找针入度试验数据
     * 使用原生SQL查询避免Hibernate自动类型转换
     */
    @Query(value = "SELECT apt.*, u.name as experimenter_name FROM asphalt_penetration_test apt " +
                   "LEFT JOIN users u ON apt.experimenter = u.username " +
                   "WHERE apt.task_id = :taskId", nativeQuery = true)
    Optional<PenetrationTest> findByTaskIdNative(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID查找所有针入度试验数据
     */
    @Query(value = "SELECT apt.*, u.name as experimenter_name FROM asphalt_penetration_test apt " +
                   "LEFT JOIN users u ON apt.experimenter = u.username " +
                   "WHERE apt.task_id = :taskId", nativeQuery = true)
    List<PenetrationTest> findAllByTaskIdNative(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID前缀查找匹配的针入度试验数据
     * 适用于部分ID匹配的场景
     */
    @Query(value = "SELECT apt.*, u.name as experimenter_name FROM asphalt_penetration_test apt " +
                   "LEFT JOIN users u ON apt.experimenter = u.username " +
                   "WHERE apt.task_id LIKE CONCAT(:taskIdPrefix, '%')", 
           nativeQuery = true)
    List<PenetrationTest> findAllByTaskIdPrefixNative(@Param("taskIdPrefix") String taskIdPrefix);
    
    /**
     * 查找指定实验操作人的针入度试验数据
     */
    @Query(value = "SELECT apt.*, u.name as experimenter_name FROM asphalt_penetration_test apt " +
                   "LEFT JOIN users u ON apt.experimenter = u.username " +
                   "WHERE apt.experimenter = :experimenter", 
           nativeQuery = true)
    List<PenetrationTest> findAllByExperimenterNative(@Param("experimenter") String experimenter);
}
