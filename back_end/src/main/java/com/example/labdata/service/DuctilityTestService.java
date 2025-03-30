package com.example.labdata.service;

import com.example.labdata.model.DuctilityTest;
import com.example.labdata.payload.request.DuctilityTestRequest;
import com.example.labdata.repository.DuctilityTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 延度实验服务类
 */
@Service
public class DuctilityTestService {
    private static final Logger logger = LoggerFactory.getLogger(DuctilityTestService.class);

    @Autowired
    private DuctilityTestRepository ductilityTestRepository;

    /**
     * 保存延度实验数据
     * @param request 延度实验数据请求
     * @return 保存的实验数据
     */
    @Transactional
    public DuctilityTest saveDuctilityTestData(DuctilityTestRequest request) {
        DuctilityTest ductilityTest = new DuctilityTest();
        
        // 使用String类型保存taskId，避免大数值问题
        ductilityTest.setTaskId(request.getTaskId());
        ductilityTest.setTemperature(request.getTemperature());
        ductilityTest.setDisplacement(request.getDisplacement());
        ductilityTest.setExperimenter(request.getExperimenter());
        ductilityTest.setTestDate(request.getTestDate());
        ductilityTest.setDeviceId(request.getDeviceId());
        ductilityTest.setDeviceName(request.getDeviceName());
        ductilityTest.setDeviceManufacturer(request.getDeviceManufacturer());
        ductilityTest.setDeviceModel(request.getDeviceModel());
        
        logger.info("保存延度实验数据，任务ID: {}", request.getTaskId());
        return ductilityTestRepository.save(ductilityTest);
    }

    /**
     * 根据任务ID获取延度实验数据
     * @param taskId 任务ID
     * @return 延度实验数据列表
     */
    public List<DuctilityTest> getDuctilityTestDataByTaskId(String taskId) {
        logger.info("获取延度实验数据，任务ID: {}, 使用关联用户表查询以获取操作者真实姓名", taskId);
        return ductilityTestRepository.findByTaskIdWithExperimenterName(taskId);
    }
}
