package com.example.labdata.repository;

import com.example.labdata.entity.BrookfieldViscosityMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验粘度测量值存储库接口
 */
@Repository
public interface BrookfieldViscosityMeasurementRepository extends JpaRepository<BrookfieldViscosityMeasurement, Long> {
    
    /**
     * 根据温度点ID查找所有粘度测量值
     */
    List<BrookfieldViscosityMeasurement> findByTemperaturePointId(Long temperaturePointId);
    
    /**
     * 根据温度点ID和测量值ID查找粘度测量值
     */
    BrookfieldViscosityMeasurement findByTemperaturePointIdAndMeasurementId(Long temperaturePointId, String measurementId);
}
