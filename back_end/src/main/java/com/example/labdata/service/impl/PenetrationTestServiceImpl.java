package com.example.labdata.service.impl;

import com.example.labdata.model.PenetrationTest;
import com.example.labdata.payload.request.PenetrationTestRequest;
import com.example.labdata.repository.PenetrationTestRepository;
import com.example.labdata.service.PenetrationTestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 针入度试验服务实现类
 */
@Service
public class PenetrationTestServiceImpl implements PenetrationTestService {
    
    private static final Logger logger = LoggerFactory.getLogger(PenetrationTestServiceImpl.class);
    
    @Autowired
    private PenetrationTestRepository penetrationTestRepository;
    
    @Override
    public boolean submitPenetrationTest(PenetrationTestRequest request) {
        try {
            logger.info("正在保存针入度试验数据, 任务ID: {}", request.getTaskId());
            
            PenetrationTest penetrationTest = new PenetrationTest();
            
            // 使用String类型存储taskId，避免大数值问题
            penetrationTest.setTaskId(request.getTaskId());
            penetrationTest.setTemperature(request.getTemperature());
            penetrationTest.setReading(request.getReading());
            penetrationTest.setExperimenter(request.getExperimenter());
            
            // 设置试验日期 - 直接使用毫秒时间戳
            if (request.getTestDate() != null) {
                penetrationTest.setTestDate(request.getTestDate());
            } else {
                penetrationTest.setTestDate(System.currentTimeMillis());
            }
            
            // 设置设备信息
            penetrationTest.setDeviceId(request.getDeviceId());
            penetrationTest.setDeviceName(request.getDeviceName());
            penetrationTest.setDeviceManufacturer(request.getDeviceManufacturer());
            penetrationTest.setDeviceModel(request.getDeviceModel());
            
            // 保存数据
            penetrationTestRepository.save(penetrationTest);
            logger.info("针入度试验数据保存成功");
            return true;
        } catch (Exception e) {
            logger.error("保存针入度试验数据时发生错误: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Optional<PenetrationTest> getPenetrationTestByTaskId(String taskId) {
        logger.info("正在查询针入度试验数据, 任务ID: {}", taskId);
        try {
            // 使用原生SQL查询避免数据类型转换问题
            return penetrationTestRepository.findByTaskIdNative(taskId);
        } catch (Exception e) {
            logger.error("查询针入度试验数据失败: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    @Override
    public List<PenetrationTest> getAllPenetrationTests() {
        logger.info("正在查询所有针入度试验数据");
        return penetrationTestRepository.findAll();
    }
}
