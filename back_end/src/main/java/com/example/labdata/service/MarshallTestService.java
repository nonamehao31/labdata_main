package com.example.labdata.service;

import com.example.labdata.model.MarshallTest;
import com.example.labdata.payload.request.MarshallTestRequest;
import com.example.labdata.repository.MarshallTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 马歇尔试验数据服务
 */
@Service
public class MarshallTestService {

    private static final Logger logger = LoggerFactory.getLogger(MarshallTestService.class);

    private final MarshallTestRepository marshallTestRepository;

    @Autowired
    public MarshallTestService(MarshallTestRepository marshallTestRepository) {
        this.marshallTestRepository = marshallTestRepository;
    }

    /**
     * 保存马歇尔试验数据
     *
     * @param request 马歇尔试验数据请求
     * @return 保存的马歇尔试验数据
     */
    public MarshallTest saveMarshallTest(MarshallTestRequest request) {
        logger.info("保存马歇尔试验数据，任务ID: {}", request.getTaskId());
        
        // 查找是否已经存在该任务的马歇尔实验数据
        List<MarshallTest> existingTests = marshallTestRepository.findByTaskId(request.getTaskId());
        
        MarshallTest test;
        if (!existingTests.isEmpty()) {
            // 更新现有记录
            test = existingTests.get(0);
            logger.info("更新现有马歇尔试验数据，ID: {}", test.getId());
        } else {
            // 创建新记录
            test = new MarshallTest();
            test.setTaskId(request.getTaskId());
            logger.info("创建新的马歇尔试验数据记录");
        }
        
        // 设置测试数据
        test.setStability1(request.getStability1());
        test.setStreamValue1(request.getStreamValue1());
        test.setStability2(request.getStability2());
        test.setStreamValue2(request.getStreamValue2());
        test.setStability3(request.getStability3());
        test.setStreamValue3(request.getStreamValue3());
        
        // 保存到数据库
        return marshallTestRepository.save(test);
    }
    
    /**
     * 根据任务ID获取马歇尔试验数据
     *
     * @param taskId 任务ID
     * @return 马歇尔试验数据列表
     */
    public List<MarshallTest> getMarshallTestsByTaskId(String taskId) {
        logger.info("获取任务ID: {} 的马歇尔试验数据", taskId);
        return marshallTestRepository.findAllByTaskIdNative(taskId);
    }
}
