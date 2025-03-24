package com.example.labdata.service;

import com.example.labdata.model.SofteningPointTest;
import com.example.labdata.payload.request.SofteningPointTestRequest;

import java.util.List;
import java.util.Optional;

/**
 * 软化点试验服务接口
 */
public interface SofteningPointTestService {
    
    /**
     * 提交软化点试验数据
     * 
     * @param request 软化点试验数据请求
     * @return 是否提交成功
     */
    boolean submitSofteningPointTest(SofteningPointTestRequest request);
    
    /**
     * 根据任务ID获取软化点试验记录
     * 
     * @param taskId 任务ID
     * @return 软化点试验记录
     */
    Optional<SofteningPointTest> getSofteningPointTestByTaskId(String taskId);
    
    /**
     * 获取所有软化点试验记录
     * 
     * @return 所有软化点试验记录
     */
    List<SofteningPointTest> getAllSofteningPointTests();
    
    /**
     * 获取指定操作人的所有软化点试验记录
     * 
     * @param experimenter 操作人
     * @return 软化点试验记录列表
     */
    List<SofteningPointTest> getSofteningPointTestsByExperimenter(String experimenter);
    
    /**
     * 获取指定软化温度范围的所有试验记录
     * 
     * @param minTemp 最小软化温度
     * @param maxTemp 最大软化温度
     * @return 软化点试验记录列表
     */
    List<SofteningPointTest> getSofteningPointTestsByTemperatureRange(double minTemp, double maxTemp);
}
