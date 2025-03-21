package com.example.labdata.service;

import com.example.labdata.model.HamburgRuttingTest;
import com.example.labdata.repository.HamburgRuttingTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 汉堡车辙实验数据服务
 */
@Service
public class HamburgRuttingTestService {

    private final HamburgRuttingTestRepository hamburgRuttingTestRepository;

    @Autowired
    public HamburgRuttingTestService(HamburgRuttingTestRepository hamburgRuttingTestRepository) {
        this.hamburgRuttingTestRepository = hamburgRuttingTestRepository;
    }

    /**
     * 保存汉堡车辙实验数据
     *
     * @param hamburgRuttingTest 要保存的数据
     * @return 已保存的数据
     */
    public HamburgRuttingTest saveHamburgRuttingTest(HamburgRuttingTest hamburgRuttingTest) {
        // 检查是否已存在该任务和配比的数据
        Optional<HamburgRuttingTest> existingData = hamburgRuttingTestRepository
            .findByTaskIdAndMixRatioId(hamburgRuttingTest.getTaskId(), hamburgRuttingTest.getMixRatioId());
        
        // 如果存在，则更新数据而不是创建新记录
        if (existingData.isPresent()) {
            HamburgRuttingTest existing = existingData.get();
            existing.setSteadySlope1(hamburgRuttingTest.getSteadySlope1());
            existing.setSteadyCurvilinear1(hamburgRuttingTest.getSteadyCurvilinear1());
            existing.setSteadySlope2(hamburgRuttingTest.getSteadySlope2());
            existing.setSteadyCurvilinear2(hamburgRuttingTest.getSteadyCurvilinear2());
            return hamburgRuttingTestRepository.save(existing);
        }
        
        // 否则创建新记录
        return hamburgRuttingTestRepository.save(hamburgRuttingTest);
    }

    /**
     * 根据任务ID获取汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @return 汉堡车辙实验数据列表
     */
    public List<HamburgRuttingTest> getHamburgRuttingTestsByTaskId(String taskId) {
        return hamburgRuttingTestRepository.findByTaskId(taskId);
    }

    /**
     * 根据任务ID和配比ID获取汉堡车辙实验数据
     *
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 汉堡车辙实验数据，如果不存在则返回null
     */
    public HamburgRuttingTest getHamburgRuttingTestByTaskIdAndMixRatioId(String taskId, Long mixRatioId) {
        return hamburgRuttingTestRepository.findByTaskIdAndMixRatioId(taskId, mixRatioId).orElse(null);
    }
}
