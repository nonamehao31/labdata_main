package com.example.labdata.repository;

import com.example.labdata.model.MixtureBendingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 沥青混合料弯曲试验数据访问接口
 */
@Repository
public interface MixtureBendingTestRepository extends JpaRepository<MixtureBendingTest, Long> {
    
    /**
     * 根据任务ID查找所有弯曲试验数据
     * 
     * @param taskId 任务ID
     * @return 指定任务的所有弯曲试验数据列表
     */
    List<MixtureBendingTest> findByTaskId(String taskId);
    
    /**
     * 根据任务ID和配比ID查找特定的弯曲试验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 指定任务和配比的弯曲试验数据，如果不存在则返回空
     */
    Optional<MixtureBendingTest> findByTaskIdAndMixRatioId(String taskId, Long mixRatioId);
}
