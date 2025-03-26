package com.example.labdata.service;

import com.example.labdata.entity.DsrMeasurement;
import com.example.labdata.entity.DsrTemperaturePoint;
import com.example.labdata.entity.DynamicShearRheometerTest;
import com.example.labdata.payload.dto.DsrDataPoint;
import com.example.labdata.payload.dto.DsrTestResult;
import com.example.labdata.payload.request.DynamicShearRheometerTestRequest;
import com.example.labdata.repository.DsrMeasurementRepository;
import com.example.labdata.repository.DsrTemperaturePointRepository;
import com.example.labdata.repository.DynamicShearRheometerTestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态剪切流变仪实验服务
 */
@Service
public class DynamicShearRheometerTestService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicShearRheometerTestService.class);

    @Autowired
    private DynamicShearRheometerTestRepository testRepository;

    @Autowired
    private DsrTemperaturePointRepository temperaturePointRepository;

    @Autowired
    private DsrMeasurementRepository measurementRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Pattern TEMPERATURE_PATTERN = Pattern.compile("temperature_(\\d+)");

    /**
     * 保存动态剪切流变仪实验数据
     *
     * @param request 请求数据
     * @return 保存的实验实体
     */
    @Transactional
    public DynamicShearRheometerTest saveTestData(DynamicShearRheometerTestRequest request) {
        try {
            // 1. 保存主表数据
            DynamicShearRheometerTest test = new DynamicShearRheometerTest();
            test.setTaskId(request.getTaskId());
            test.setOperatorId(request.getOperatorId());
            test.setSpecimenId(request.getSpecimenId());
            test.setSpecimenType(request.getSpecimenType());
            test.setMaterialType(request.getMaterialType());
            test.setControlMode(request.getControlMode());
            test.setTestRadius(request.getTestRadius());
            test.setPlateGap(request.getPlateGap());
            test.setRemarks(request.getRemarks());

            // 将实验参数转换为JSON字符串作为备份
            if (request.getExperimentValues() != null) {
                String experimentValuesJson = objectMapper.writeValueAsString(request.getExperimentValues());
                test.setExperimentValues(experimentValuesJson);
            }

            // 保存主表数据并获取ID
            DynamicShearRheometerTest savedTest = testRepository.save(test);

            // 2. 解析并保存温度点数据
            Map<String, String> experimentValues = request.getExperimentValues();
            if (experimentValues != null) {
                // 找出所有温度点 (temperature_1, temperature_2, ...)
                List<Integer> tempPointIndexes = new ArrayList<>();
                for (String key : experimentValues.keySet()) {
                    Matcher matcher = TEMPERATURE_PATTERN.matcher(key);
                    if (matcher.matches()) {
                        tempPointIndexes.add(Integer.parseInt(matcher.group(1)));
                    }
                }

                // 对于每个温度点
                for (Integer index : tempPointIndexes) {
                    // 创建温度点记录
                    DsrTemperaturePoint temperaturePoint = new DsrTemperaturePoint();
                    temperaturePoint.setTestId(savedTest.getId());
                    temperaturePoint.setPointNumber(index);

                    String tempKey = "temperature_" + index;
                    if (experimentValues.containsKey(tempKey)) {
                        try {
                            temperaturePoint.setTemperature(Double.parseDouble(experimentValues.get(tempKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    // 保存温度点并获取ID
                    DsrTemperaturePoint savedTempPoint = temperaturePointRepository.save(temperaturePoint);

                    // 3. 创建该温度点对应的测量值记录
                    DsrMeasurement measurement = new DsrMeasurement();
                    measurement.setTemperaturePointId(savedTempPoint.getId());

                    // 解析并设置该温度点的测量值
                    String freqKey = "frequency_" + index;
                    String stressKey = "max_shear_stress_" + index;
                    String strainKey = "max_shear_strain_" + index;
                    String angleKey = "phase_angle_" + index;
                    String modulusKey = "complex_shear_modulus_" + index;

                    if (experimentValues.containsKey(freqKey)) {
                        try {
                            measurement.setLoadFrequency(Double.parseDouble(experimentValues.get(freqKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    if (experimentValues.containsKey(stressKey)) {
                        try {
                            measurement.setMaxShearStress(Double.parseDouble(experimentValues.get(stressKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    if (experimentValues.containsKey(strainKey)) {
                        try {
                            measurement.setMaxShearStrain(Double.parseDouble(experimentValues.get(strainKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    if (experimentValues.containsKey(angleKey)) {
                        try {
                            measurement.setPhaseAngle(Double.parseDouble(experimentValues.get(angleKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    if (experimentValues.containsKey(modulusKey)) {
                        try {
                            measurement.setComplexShearModulus(Double.parseDouble(experimentValues.get(modulusKey)));
                        } catch (NumberFormatException e) {
                            // 忽略非数字值
                        }
                    }

                    // 保存测量值记录
                    measurementRepository.save(measurement);
                }
            }

            return savedTest;
        } catch (Exception e) {
            throw new RuntimeException("保存动态剪切流变仪实验数据失败", e);
        }
    }

    /**
     * 根据任务ID获取实验数据
     *
     * @param taskId 任务ID
     * @return 实验数据结果列表
     */
    public List<DsrTestResult> getTestDataByTaskId(String taskId) {
        // 1. 查询主表获取实验基本信息
        List<DynamicShearRheometerTest> testList = testRepository.findByTaskId(taskId);
        List<DsrTestResult> resultList = new ArrayList<>();
        
        for (DynamicShearRheometerTest test : testList) {
            DsrTestResult result = new DsrTestResult();
            
            // 复制基本信息
            result.setId(test.getId());
            result.setTaskId(test.getTaskId());
            result.setOperatorId(test.getOperatorId());
            result.setSpecimenId(test.getSpecimenId());
            result.setSpecimenType(test.getSpecimenType());
            result.setMaterialType(test.getMaterialType());
            result.setControlMode(test.getControlMode());
            result.setTestRadius(test.getTestRadius());
            result.setPlateGap(test.getPlateGap());
            result.setRemarks(test.getRemarks());
            
            // 2. 查询该实验的所有温度点
            List<DsrTemperaturePoint> tempPoints = temperaturePointRepository.findByTestId(test.getId());
            List<DsrDataPoint> dataPoints = new ArrayList<>();
            
            for (DsrTemperaturePoint tempPoint : tempPoints) {
                // 3. 查询每个温度点的所有测量值
                List<DsrMeasurement> measurements = measurementRepository.findByTemperaturePointId(tempPoint.getId());
                
                for (DsrMeasurement measurement : measurements) {
                    DsrDataPoint dataPoint = new DsrDataPoint();
                    
                    // 设置温度和频率
                    dataPoint.setTemperature(tempPoint.getTemperature());
                    dataPoint.setFrequency(measurement.getLoadFrequency());
                    
                    // 设置原始测量值
                    dataPoint.setMaxShearStress(measurement.getMaxShearStress());
                    dataPoint.setMaxShearStrain(measurement.getMaxShearStrain());
                    dataPoint.setPhaseAngle(measurement.getPhaseAngle());
                    
                    // 直接使用数据库中存储的复合剪切模量值
                    dataPoint.setComplexModulus(measurement.getComplexShearModulus());
                    
                    // 如果数据库中的复合剪切模量为空，则尝试计算
                    if (dataPoint.getComplexModulus() == null && 
                        measurement.getMaxShearStrain() != null && 
                        measurement.getMaxShearStrain() != 0 &&
                        measurement.getMaxShearStress() != null) {
                        double calculatedModulus = measurement.getMaxShearStress() / measurement.getMaxShearStrain();
                        dataPoint.setComplexModulus(calculatedModulus);
                        logger.info("计算复合剪切模量: {}", calculatedModulus);
                    }
                    
                    dataPoints.add(dataPoint);
                }
            }
            
            // 4. 将所有数据点设置到结果对象中
            result.setDataPoints(dataPoints);
            resultList.add(result);
        }
        
        logger.info("根据任务ID获取DSR实验数据: 找到{}个实验, 共{}个数据点", 
                resultList.size(), 
                resultList.stream().mapToInt(r -> r.getDataPoints().size()).sum());
        
        return resultList;
    }
}