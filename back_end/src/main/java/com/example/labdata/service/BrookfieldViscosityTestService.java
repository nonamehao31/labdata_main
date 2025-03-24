package com.example.labdata.service;

import com.example.labdata.entity.BrookfieldViscosityMeasurement;
import com.example.labdata.entity.BrookfieldViscosityTemperaturePoint;
import com.example.labdata.entity.BrookfieldViscosityTest;
import com.example.labdata.payload.request.BrookfieldViscosityTestRequest;
import com.example.labdata.repository.BrookfieldViscosityMeasurementRepository;
import com.example.labdata.repository.BrookfieldViscosityTemperaturePointRepository;
import com.example.labdata.repository.BrookfieldViscosityTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 布鲁克菲尔德旋转黏度实验服务类
 */
@Service
public class BrookfieldViscosityTestService {
    
    private final BrookfieldViscosityTestRepository testRepository;
    private final BrookfieldViscosityTemperaturePointRepository temperaturePointRepository;
    private final BrookfieldViscosityMeasurementRepository measurementRepository;
    
    // 定义正则表达式模式
    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("temperature_(\\w+)");
    private static final Pattern VISCOSITY_PATTERN = Pattern.compile("viscosity_(\\w+)_(\\w+)");
    private static final Pattern SPINDLE_TYPE_PATTERN = Pattern.compile("spindle_type_(\\w+)_(\\w+)");
    private static final Pattern ROTATION_SPEED_PATTERN = Pattern.compile("rotation_speed_(\\w+)_(\\w+)");
    
    @Autowired
    public BrookfieldViscosityTestService(
            BrookfieldViscosityTestRepository testRepository,
            BrookfieldViscosityTemperaturePointRepository temperaturePointRepository,
            BrookfieldViscosityMeasurementRepository measurementRepository) {
        this.testRepository = testRepository;
        this.temperaturePointRepository = temperaturePointRepository;
        this.measurementRepository = measurementRepository;
    }
    
    /**
     * 保存布鲁克菲尔德旋转黏度实验数据
     */
    @Transactional
    public BrookfieldViscosityTest saveTestData(BrookfieldViscosityTestRequest request) {
        // 创建并保存测试实体
        BrookfieldViscosityTest test = new BrookfieldViscosityTest();
        test.setTaskId(request.getTaskId());
        test.setExperimenter(request.getExperimenter());
        test.setTestDate(request.getTestDate());
        test.setDeviceId(request.getDeviceId());
        test.setDeviceName(request.getDeviceName());
        test.setDeviceManufacturer(request.getDeviceManufacturer());
        test.setDeviceModel(request.getDeviceModel());
        
        BrookfieldViscosityTest savedTest = testRepository.save(test);
        
        // 处理experimentValues中的数据，提取温度点和测量值
        Map<String, String> experimentValues = request.getExperimentValues();
        if (experimentValues != null && !experimentValues.isEmpty()) {
            // 存储温度点数据，键为pointId，值为温度值
            Map<String, String> temperaturePoints = new HashMap<>();
            // 存储粘度测量值数据
            Map<String, Map<String, String>> viscosityMeasurements = new HashMap<>();
            
            // 存储全局的转子类型和转速 (处理前端直接发送的简单格式字段)
            String globalRotorType = null;
            String globalRotationSpeed = null;
            
            // 解析实验值映射
            for (Map.Entry<String, String> entry : experimentValues.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                
                // 处理前端直接发送的简单格式字段
                if (key.equals("rotor_type") || key.equals("spindle_type")) {
                    globalRotorType = value;
                    continue;
                }
                
                if (key.equals("rotation_speed")) {
                    globalRotationSpeed = value;
                    continue;
                }
                
                // 尝试匹配温度点
                Matcher tempMatcher = TEMPERATURE_PATTERN.matcher(key);
                if (tempMatcher.matches()) {
                    String pointId = tempMatcher.group(1);
                    temperaturePoints.put(pointId, value);
                    continue;
                }
                
                // 尝试匹配粘度值
                Matcher viscosityMatcher = VISCOSITY_PATTERN.matcher(key);
                if (viscosityMatcher.matches()) {
                    String pointId = viscosityMatcher.group(1);
                    String measurementId = viscosityMatcher.group(2);
                    
                    // 确保pointId的映射存在
                    viscosityMeasurements.computeIfAbsent(pointId, k -> new HashMap<>())
                            .put("viscosity_" + measurementId, value);
                    continue;
                }
                
                // 尝试匹配转子型号
                Matcher spindleMatcher = SPINDLE_TYPE_PATTERN.matcher(key);
                if (spindleMatcher.matches()) {
                    String pointId = spindleMatcher.group(1);
                    String measurementId = spindleMatcher.group(2);
                    
                    viscosityMeasurements.computeIfAbsent(pointId, k -> new HashMap<>())
                            .put("spindle_type_" + measurementId, value);
                    continue;
                }
                
                // 尝试匹配转速
                Matcher speedMatcher = ROTATION_SPEED_PATTERN.matcher(key);
                if (speedMatcher.matches()) {
                    String pointId = speedMatcher.group(1);
                    String measurementId = speedMatcher.group(2);
                    
                    viscosityMeasurements.computeIfAbsent(pointId, k -> new HashMap<>())
                            .put("rotation_speed_" + measurementId, value);
                }
            }
            
            // 创建温度点实体并保存
            for (Map.Entry<String, String> pointEntry : temperaturePoints.entrySet()) {
                String pointId = pointEntry.getKey();
                String temperature = pointEntry.getValue();
                
                BrookfieldViscosityTemperaturePoint temperaturePoint = new BrookfieldViscosityTemperaturePoint();
                temperaturePoint.setPointId(pointId);
                temperaturePoint.setTemperature(temperature);
                temperaturePoint.setTest(savedTest);
                
                BrookfieldViscosityTemperaturePoint savedPoint = temperaturePointRepository.save(temperaturePoint);
                
                // 处理该温度点下的粘度测量值
                if (viscosityMeasurements.containsKey(pointId)) {
                    Map<String, String> measurements = viscosityMeasurements.get(pointId);
                    Map<String, Map<String, String>> measurementGroups = groupMeasurements(measurements);
                    
                    for (Map.Entry<String, Map<String, String>> measurementEntry : measurementGroups.entrySet()) {
                        String measurementId = measurementEntry.getKey();
                        Map<String, String> measurementData = measurementEntry.getValue();
                        
                        BrookfieldViscosityMeasurement measurement = new BrookfieldViscosityMeasurement();
                        measurement.setMeasurementId(measurementId);
                        measurement.setTemperaturePoint(savedPoint);
                        
                        // 设置粘度值
                        if (measurementData.containsKey("viscosity_" + measurementId)) {
                            measurement.setViscosity(measurementData.get("viscosity_" + measurementId));
                        }
                        
                        // 设置转子型号（可选）- 优先使用特定测量值的设置，如果没有则使用全局设置
                        if (measurementData.containsKey("spindle_type_" + measurementId)) {
                            measurement.setSpindleType(measurementData.get("spindle_type_" + measurementId));
                        } else if (globalRotorType != null) {
                            measurement.setSpindleType(globalRotorType);
                        }
                        
                        // 设置转速（可选）- 优先使用特定测量值的设置，如果没有则使用全局设置
                        if (measurementData.containsKey("rotation_speed_" + measurementId)) {
                            measurement.setRotationSpeed(measurementData.get("rotation_speed_" + measurementId));
                        } else if (globalRotationSpeed != null) {
                            measurement.setRotationSpeed(globalRotationSpeed);
                        }
                        
                        measurementRepository.save(measurement);
                    }
                }
            }
        }
        
        return savedTest;
    }
    
    /**
     * 将测量值数据分组，按测量值ID归类
     */
    private Map<String, Map<String, String>> groupMeasurements(Map<String, String> measurements) {
        Map<String, Map<String, String>> groups = new HashMap<>();
        
        for (Map.Entry<String, String> entry : measurements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // 提取测量值ID
            String measurementId = null;
            if (key.startsWith("viscosity_")) {
                measurementId = key.substring("viscosity_".length());
            } else if (key.startsWith("spindle_type_")) {
                measurementId = key.substring("spindle_type_".length());
            } else if (key.startsWith("rotation_speed_")) {
                measurementId = key.substring("rotation_speed_".length());
            }
            
            if (measurementId != null) {
                groups.computeIfAbsent(measurementId, k -> new HashMap<>()).put(key, value);
            }
        }
        
        return groups;
    }
    
    /**
     * 获取指定任务的所有布鲁克菲尔德旋转黏度实验数据
     */
    public List<BrookfieldViscosityTest> getTestsByTaskId(String taskId) {
        try {
            // 尝试使用原生SQL查询避免Long类型转换问题
            return testRepository.findByTaskId(taskId);
        } catch (Exception e) {
            // 如果出错，记录错误并返回空列表
            System.err.println("Error retrieving tests by task ID: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
