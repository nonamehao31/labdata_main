package com.example.labdata.repository;

import com.example.labdata.model.HamburgRuttingTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 汉堡车辙实验数据访问接口
 */
@Repository
public interface HamburgRuttingTestRepository extends JpaRepository<HamburgRuttingTest, Long> {
    
    /**
     * 根据任务ID查询汉堡车辙实验数据
     * 
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据列表
     */
    List<HamburgRuttingTest> findByTaskId(String taskId);
    
    /**
     * 根据任务ID和配比ID查询汉堡车辙实验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 汉堡车辙实验数据
     */
    Optional<HamburgRuttingTest> findByTaskIdAndMixRatioId(String taskId, Long mixRatioId);
}
