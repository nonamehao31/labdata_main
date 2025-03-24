package com.example.labdata.repository;

import com.example.labdata.entity.BbrTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 沥青弯曲蠕变劲度试验存储库接口
 */
@Repository
public interface BbrTestRepository extends JpaRepository<BbrTest, Long> {
    
    /**
     * 根据任务ID查找实验数据
     */
    List<BbrTest> findByTaskId(String taskId);
    
    /**
     * 通过原生SQL查询避免Long类型转换问题
     */
    @Query(value = "SELECT * FROM bbr_test WHERE task_id = :taskId", nativeQuery = true)
    List<BbrTest> findByTaskIdNative(@Param("taskId") String taskId);
    
    /**
     * 根据ID和任务ID查找实验数据
     */
    Optional<BbrTest> findByIdAndTaskId(Long id, String taskId);
}
