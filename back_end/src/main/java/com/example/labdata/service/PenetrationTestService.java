package com.example.labdata.service;

import com.example.labdata.model.PenetrationTest;
import com.example.labdata.payload.request.PenetrationTestRequest;
import com.example.labdata.repository.PenetrationTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 针入度试验服务接口
 */
public interface PenetrationTestService {
    
    /**
     * 提交针入度试验数据
     * @param request 试验数据请求
     * @return 是否保存成功
     */
    boolean submitPenetrationTest(PenetrationTestRequest request);
    
    /**
     * 根据任务ID获取针入度试验数据
     * @param taskId 任务ID
     * @return 试验数据
     */
    Optional<PenetrationTest> getPenetrationTestByTaskId(String taskId);
    
    /**
     * 获取所有针入度试验数据
     * @return 试验数据列表
     */
    List<PenetrationTest> getAllPenetrationTests();
}
