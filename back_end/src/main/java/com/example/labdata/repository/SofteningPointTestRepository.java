package com.example.labdata.repository;

import com.example.labdata.model.SofteningPointTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 软化点试验数据仓库
 */
@Repository
public interface SofteningPointTestRepository extends JpaRepository<SofteningPointTest, Long> {
    
    /**
     * 根据任务ID查询软化点试验记录
     * 使用原生SQL查询避免类型转换问题
     * 
     * @param taskId 任务ID
     * @return 软化点试验记录
     */
    @Query(value = "SELECT spt.*, u.name as experimenter_name FROM asphalt_softening_point_test spt " +
                  "LEFT JOIN users u ON spt.experimenter = u.username " +
                  "WHERE spt.task_id = :taskId", nativeQuery = true)
    Optional<SofteningPointTest> findByTaskIdNative(@Param("taskId") String taskId);
    
    /**
     * 查询指定操作人的所有软化点试验记录
     * 
     * @param experimenter 操作人
     * @return 软化点试验记录列表
     */
    @Query(value = "SELECT spt.*, u.name as experimenter_name FROM asphalt_softening_point_test spt " +
                  "LEFT JOIN users u ON spt.experimenter = u.username " +
                  "WHERE spt.experimenter = :experimenter", nativeQuery = true)
    List<SofteningPointTest> findByExperimenterNative(@Param("experimenter") String experimenter);
    
    /**
     * 查询指定温度范围内的软化点试验记录
     * 
     * @param minTemp 最小软化温度
     * @param maxTemp 最大软化温度
     * @return 软化点试验记录列表
     */
    @Query(value = "SELECT spt.*, u.name as experimenter_name FROM asphalt_softening_point_test spt " +
                  "LEFT JOIN users u ON spt.experimenter = u.username " +
                  "WHERE CAST(spt.softening_temperature AS DECIMAL) BETWEEN :minTemp AND :maxTemp", nativeQuery = true)
    List<SofteningPointTest> findBySofteningTemperatureRangeNative(@Param("minTemp") double minTemp, @Param("maxTemp") double maxTemp);
}
