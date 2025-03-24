package com.example.labdata.repository;

import com.example.labdata.model.DuctilityTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 延度实验数据仓库接口
 */
@Repository
public interface DuctilityTestRepository extends JpaRepository<DuctilityTest, Long> {
    
    /**
     * 根据任务ID查找延度实验数据
     * @param taskId 任务ID（String类型以支持超长ID）
     * @return 延度实验数据列表
     */
    List<DuctilityTest> findByTaskId(String taskId);
}
