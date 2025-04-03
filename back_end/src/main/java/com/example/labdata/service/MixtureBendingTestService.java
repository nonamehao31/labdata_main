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
        
        // 首先尝试使用原始任务ID查询
        List<MixtureBendingTest> results = mixtureBendingTestRepository.findByTaskId(taskId);
        
        // 如果没有结果，并且任务ID包含后缀，尝试移除后缀再查询
        if (results.isEmpty() && taskId.contains("-")) {
            // 获取基础任务ID（移除最后一个'-'及之后的内容）
            String baseTaskId = taskId.substring(0, taskId.lastIndexOf("-"));
            logger.info("未找到数据，尝试使用基础任务ID {} 重新查询", baseTaskId);
            
            results = mixtureBendingTestRepository.findByTaskId(baseTaskId);
            
            // 如果仍然没有结果，尝试使用前缀匹配
            if (results.isEmpty()) {
                // 使用UUID部分作为前缀(通常是任务ID的前36个字符)
                String taskIdPrefix = taskId.length() > 36 ? taskId.substring(0, 36) : taskId;
                logger.info("基础ID查询仍未找到数据，尝试使用前缀匹配: {}", taskIdPrefix);
                
                results = mixtureBendingTestRepository.findByTaskIdStartingWith(taskIdPrefix);
                
                if (!results.isEmpty()) {
                    logger.info("使用前缀匹配成功找到 {} 条数据", results.size());
                }
            } else {
                // 如果找到了结果，记录日志
                logger.info("使用基础任务ID {} 成功找到 {} 条数据", baseTaskId, results.size());
            }
        }
        
        return results;
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
        
        // 首先尝试使用原始任务ID查询
        Optional<MixtureBendingTest> result = mixtureBendingTestRepository.findByTaskIdAndMixRatioId(taskId, mixRatioId);
        
        // 如果没有结果，并且任务ID包含后缀，尝试移除后缀再查询
        if (!result.isPresent() && taskId.contains("-")) {
            // 获取基础任务ID（移除最后一个'-'及之后的内容）
            String baseTaskId = taskId.substring(0, taskId.lastIndexOf("-"));
            logger.info("未找到数据，尝试使用基础任务ID {} 重新查询", baseTaskId);
            
            result = mixtureBendingTestRepository.findByTaskIdAndMixRatioId(baseTaskId, mixRatioId);
            
            // 如果仍然没有结果，尝试使用前缀匹配
            if (!result.isPresent()) {
                // 使用UUID部分作为前缀(通常是任务ID的前36个字符)
                String taskIdPrefix = taskId.length() > 36 ? taskId.substring(0, 36) : taskId;
                logger.info("基础ID查询仍未找到数据，尝试使用前缀匹配: {}", taskIdPrefix);
                
                // 使用前缀匹配查找所有可能的结果，然后在内存中过滤配比ID
                List<MixtureBendingTest> prefixResults = mixtureBendingTestRepository.findByTaskIdStartingWith(taskIdPrefix);
                
                // 在结果中过滤出匹配配比ID的条目
                Optional<MixtureBendingTest> prefixResult = prefixResults.stream()
                    .filter(test -> test.getMixRatioId().equals(mixRatioId))
                    .findFirst();
                
                if (prefixResult.isPresent()) {
                    logger.info("使用前缀匹配成功找到配比ID为 {} 的数据", mixRatioId);
                    return prefixResult.get();
                }
            } else {
                logger.info("使用基础任务ID {} 成功找到配比ID为 {} 的数据", baseTaskId, mixRatioId);
                return result.get();
            }
        } else if (result.isPresent()) {
            return result.get();
        }
        
        return null;
    }
}
