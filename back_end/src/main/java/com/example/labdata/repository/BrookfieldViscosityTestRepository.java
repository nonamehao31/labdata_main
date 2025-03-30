package com.example.labdata.repository;

import com.example.labdata.entity.BrookfieldViscosityTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 布鲁克菲尔德旋转黏度实验存储库接口
 */
@Repository
public interface BrookfieldViscosityTestRepository extends JpaRepository<BrookfieldViscosityTest, Long> {
    
    /**
     * 根据任务ID查找实验数据
     */
    List<BrookfieldViscosityTest> findByTaskId(String taskId);
    
    /**
     * 通过原生SQL查询避免Long类型转换问题
     */
    @Query(value = "SELECT * FROM brookfield_viscosity_test WHERE task_id = :taskId", nativeQuery = true)
    List<BrookfieldViscosityTest> findByTaskIdNative(@Param("taskId") String taskId);
    
    /**
     * 通过原生SQL查询，并关联users表获取操作者真实姓名
     */
    @Query(value = "SELECT bvt.*, u.name as experimenter_name FROM brookfield_viscosity_test bvt " +
                   "LEFT JOIN users u ON bvt.experimenter = u.username " +
                   "WHERE bvt.task_id = :taskId", nativeQuery = true)
    List<BrookfieldViscosityTest> findByTaskIdWithExperimenterName(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID查找实验数据，同时获取所有关联的温度点和测量值
     * 使用LEFT JOIN FETCH确保即使没有关联数据也能返回主表记录
     */
    @Query("SELECT DISTINCT t FROM BrookfieldViscosityTest t " +
           "LEFT JOIN FETCH t.temperaturePoints tp " +
           "LEFT JOIN FETCH tp.measurements m " +
           "WHERE t.taskId = :taskId")
    List<BrookfieldViscosityTest> findByTaskIdWithDetails(@Param("taskId") String taskId);
    
    /**
     * 根据ID和任务ID查找实验数据
     */
    Optional<BrookfieldViscosityTest> findByIdAndTaskId(Long id, String taskId);
}
