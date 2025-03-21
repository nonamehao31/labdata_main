package com.example.labdata.service;

import com.example.labdata.model.MixtureBendingTest;
import com.example.labdata.repository.MixtureBendingTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 沥青混合料弯曲试验服务类
 */
@Service
public class MixtureBendingTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(MixtureBendingTestService.class);
    private final MixtureBendingTestRepository mixtureBendingTestRepository;
    
    public MixtureBendingTestService(MixtureBendingTestRepository mixtureBendingTestRepository) {
        this.mixtureBendingTestRepository = mixtureBendingTestRepository;
    }
    
    /**
     * 保存沥青混合料弯曲试验数据，如果已存在则更新
     * 
     * @param mixtureBendingTest 要保存的数据
     * @return 保存后的数据
     */
    public MixtureBendingTest saveMixtureBendingTest(MixtureBendingTest mixtureBendingTest) {
        logger.info("保存沥青混合料弯曲试验数据，任务ID: {}，配比ID: {}", 
                mixtureBendingTest.getTaskId(), mixtureBendingTest.getMixRatioId());
                
        Optional<MixtureBendingTest> existingData = mixtureBendingTestRepository
            .findByTaskIdAndMixRatioId(mixtureBendingTest.getTaskId(), mixtureBendingTest.getMixRatioId());
        
        if (existingData.isPresent()) {
            logger.info("找到已存在的沥青混合料弯曲试验数据，进行更新");
            MixtureBendingTest existing = existingData.get();
            existing.setSpanLength(mixtureBendingTest.getSpanLength());
            existing.setSpecimenCount(mixtureBendingTest.getSpecimenCount());
            existing.setAverageFlexuralStrength(mixtureBendingTest.getAverageFlexuralStrength());
            existing.setAverageMaxStrain(mixtureBendingTest.getAverageMaxStrain());
            existing.setAverageStiffnessModulus(mixtureBendingTest.getAverageStiffnessModulus());
            existing.setSpecimens(mixtureBendingTest.getSpecimens());
            return mixtureBendingTestRepository.save(existing);
        }
        
        logger.info("保存新的沥青混合料弯曲试验数据");
        return mixtureBendingTestRepository.save(mixtureBendingTest);
    }
    
    /**
     * 根据任务ID查询所有沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @return 指定任务的所有弯曲试验数据列表
     */
    public List<MixtureBendingTest> getMixtureBendingTestsByTaskId(String taskId) {
        logger.info("查询任务ID为 {} 的所有沥青混合料弯曲试验数据", taskId);
        return mixtureBendingTestRepository.findByTaskId(taskId);
    }
    
    /**
     * 根据任务ID和配比ID查询特定的沥青混合料弯曲试验数据
     * 
     * @param taskId 任务ID
     * @param mixRatioId 配比ID
     * @return 指定任务和配比的弯曲试验数据，如果不存在则返回null
     */
    public MixtureBendingTest getMixtureBendingTestByTaskIdAndMixRatioId(String taskId, Long mixRatioId) {
        logger.info("查询任务ID为 {}，配比ID为 {} 的沥青混合料弯曲试验数据", taskId, mixRatioId);
        return mixtureBendingTestRepository.findByTaskIdAndMixRatioId(taskId, mixRatioId).orElse(null);
    }
}
