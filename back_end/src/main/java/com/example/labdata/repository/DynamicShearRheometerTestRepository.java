package com.example.labdata.repository;

import com.example.labdata.entity.DynamicShearRheometerTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DynamicShearRheometerTestRepository extends JpaRepository<DynamicShearRheometerTest, Long> {

    /**
     * 根据任务ID查询实验数据
     * @param taskId 任务ID
     * @return 实验数据列表
     */
    List<DynamicShearRheometerTest> findByTaskId(String taskId);
}