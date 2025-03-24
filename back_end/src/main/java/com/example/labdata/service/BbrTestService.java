package com.example.labdata.service;

import com.example.labdata.entity.BbrTest;
import com.example.labdata.payload.request.BbrTestRequest;
import com.example.labdata.repository.BbrTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 沥青弯曲蠕变劲度试验（弯曲梁流变仪法）服务类
 */
@Service
public class BbrTestService {
    
    private final BbrTestRepository bbrTestRepository;
    
    @Autowired
    public BbrTestService(BbrTestRepository bbrTestRepository) {
        this.bbrTestRepository = bbrTestRepository;
    }
    
    /**
     * 保存沥青弯曲蠕变劲度试验数据
     */
    @Transactional
    public BbrTest saveTestData(BbrTestRequest request) {
        // 创建并保存测试实体
        BbrTest test = new BbrTest();
        test.setTaskId(request.getTaskId());
        test.setOperatorId(request.getOperatorId());
        test.setSpecimenId(request.getSpecimenId());
        test.setSpecimenType(request.getSpecimenType());
        test.setMaterialType(request.getMaterialType());
        test.setRemarks(request.getRemarks());
        test.setTestDate(LocalDateTime.now());
        
        // 处理实验参数
        Map<String, String> experimentValues = request.getExperimentValues();
        if (experimentValues != null) {
            for (Map.Entry<String, String> entry : experimentValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 忽略空值
                if (value == null || value.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    // 处理不同类型的参数
                    processExperimentValue(test, key, value);
                } catch (Exception e) {
                    System.err.println("处理参数时出错: " + key + " = " + value + ", 错误: " + e.getMessage());
                }
            }
        }
        
        // 保存实体
        return bbrTestRepository.save(test);
    }
    
    /**
     * 处理实验参数值
     */
    private void processExperimentValue(BbrTest test, String key, String value) {
        // 尝试将值转换为Double
        Double doubleValue = null;
        try {
            doubleValue = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // 如果不是数值，则保持字符串格式
        }
        
        // 根据键名匹配相应字段
        switch (key) {
            // 试件尺寸数据
            case "beam_span":
                test.setBeamSpan(doubleValue);
                break;
            case "specimen_width":
                test.setSpecimenWidth(doubleValue);
                break;
            case "specimen_height":
                test.setSpecimenHeight(doubleValue);
                break;
            
            // 8秒时间点数据
            case "temperature_8s":
                test.setTemperature8s(doubleValue);
                break;
            case "load_8s":
                test.setLoad8s(doubleValue);
                break;
            case "deflection_8s":
                test.setDeflection8s(doubleValue);
                break;
            case "stiffness_8s":
                test.setStiffness8s(doubleValue);
                break;
            
            // 15秒时间点数据
            case "temperature_15s":
                test.setTemperature15s(doubleValue);
                break;
            case "load_15s":
                test.setLoad15s(doubleValue);
                break;
            case "deflection_15s":
                test.setDeflection15s(doubleValue);
                break;
            case "stiffness_15s":
                test.setStiffness15s(doubleValue);
                break;
            
            // 30秒时间点数据
            case "temperature_30s":
                test.setTemperature30s(doubleValue);
                break;
            case "load_30s":
                test.setLoad30s(doubleValue);
                break;
            case "deflection_30s":
                test.setDeflection30s(doubleValue);
                break;
            case "stiffness_30s":
                test.setStiffness30s(doubleValue);
                break;
            
            // 60秒时间点数据
            case "temperature_60s":
                test.setTemperature60s(doubleValue);
                break;
            case "load_60s":
                test.setLoad60s(doubleValue);
                break;
            case "deflection_60s":
                test.setDeflection60s(doubleValue);
                break;
            case "stiffness_60s":
                test.setStiffness60s(doubleValue);
                break;
            
            // 120秒时间点数据
            case "temperature_120s":
                test.setTemperature120s(doubleValue);
                break;
            case "load_120s":
                test.setLoad120s(doubleValue);
                break;
            case "deflection_120s":
                test.setDeflection120s(doubleValue);
                break;
            case "stiffness_120s":
                test.setStiffness120s(doubleValue);
                break;
            
            // 240秒时间点数据
            case "temperature_240s":
                test.setTemperature240s(doubleValue);
                break;
            case "load_240s":
                test.setLoad240s(doubleValue);
                break;
            case "deflection_240s":
                test.setDeflection240s(doubleValue);
                break;
            case "stiffness_240s":
                test.setStiffness240s(doubleValue);
                break;
            
            // 计算结果
            case "creep_rate":
                test.setCreepRate(doubleValue);
                break;
        }
    }
    
    /**
     * 根据任务ID获取所有测试数据
     */
    public List<BbrTest> getTestsByTaskId(String taskId) {
        try {
            // 尝试使用原生SQL查询避免Long类型转换问题
            return bbrTestRepository.findByTaskId(taskId);
        } catch (Exception e) {
            // 如果出错，记录错误并返回空列表
            System.err.println("Error retrieving tests by task ID: " + e.getMessage());
            return List.of();
        }
    }
}
