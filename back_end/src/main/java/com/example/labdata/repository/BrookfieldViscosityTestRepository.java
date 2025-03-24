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
     * 根据ID和任务ID查找实验数据
     */
    Optional<BrookfieldViscosityTest> findByIdAndTaskId(Long id, String taskId);
}
