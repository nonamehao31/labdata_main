package com.example.labdata.service.impl;

import com.example.labdata.model.SofteningPointTest;
import com.example.labdata.payload.request.SofteningPointTestRequest;
import com.example.labdata.repository.SofteningPointTestRepository;
import com.example.labdata.service.SofteningPointTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 软化点试验服务实现类
 */
@Service
public class SofteningPointTestServiceImpl implements SofteningPointTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(SofteningPointTestServiceImpl.class);
    
    @Autowired
    private SofteningPointTestRepository softeningPointTestRepository;
    
    @Override
    public boolean submitSofteningPointTest(SofteningPointTestRequest request) {
        try {
            logger.info("正在保存软化点试验数据, 任务ID: {}", request.getTaskId());
            
            SofteningPointTest softeningPointTest = new SofteningPointTest();
            
            // 使用String类型存储taskId，避免大数值问题
            softeningPointTest.setTaskId(request.getTaskId());
            softeningPointTest.setTemperature(request.getTemperature());
            softeningPointTest.setSofteningTemperature(request.getSofteningTemperature());
            softeningPointTest.setExperimenter(request.getExperimenter());
            
            // 设置试验日期 - 直接使用毫秒时间戳
            if (request.getTestDate() != null) {
                softeningPointTest.setTestDate(request.getTestDate());
            } else {
                softeningPointTest.setTestDate(System.currentTimeMillis());
            }
            
            // 设置设备信息
            softeningPointTest.setDeviceId(request.getDeviceId());
            softeningPointTest.setDeviceName(request.getDeviceName());
            softeningPointTest.setDeviceManufacturer(request.getDeviceManufacturer());
            softeningPointTest.setDeviceModel(request.getDeviceModel());
            
            // 保存数据
            softeningPointTestRepository.save(softeningPointTest);
            logger.info("软化点试验数据保存成功");
            return true;
        } catch (Exception e) {
            logger.error("保存软化点试验数据时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<SofteningPointTest> getSofteningPointTestByTaskId(String taskId) {
        logger.info("正在查询软化点试验数据, 任务ID: {}", taskId);
        try {
            // 使用原生SQL查询避免数据类型转换问题
            return softeningPointTestRepository.findByTaskIdNative(taskId);
        } catch (Exception e) {
            logger.error("查询软化点试验数据失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<SofteningPointTest> getAllSofteningPointTests() {
        logger.info("正在查询所有软化点试验数据");
        return softeningPointTestRepository.findAll();
    }
    
    @Override
    public List<SofteningPointTest> getSofteningPointTestsByExperimenter(String experimenter) {
        logger.info("正在查询操作人为{}的软化点试验数据", experimenter);
        try {
            return softeningPointTestRepository.findByExperimenterNative(experimenter);
        } catch (Exception e) {
            logger.error("查询操作人为{}的软化点试验数据失败: {}", experimenter, e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<SofteningPointTest> getSofteningPointTestsByTemperatureRange(double minTemp, double maxTemp) {
        logger.info("正在查询软化温度范围在{}到{}的试验数据", minTemp, maxTemp);
        try {
            return softeningPointTestRepository.findBySofteningTemperatureRangeNative(minTemp, maxTemp);
        } catch (Exception e) {
            logger.error("查询软化温度范围在{}到{}的试验数据失败: {}", minTemp, maxTemp, e.getMessage(), e);
            return List.of();
        }
    }
}
