package com.example.labdata.repository;

import com.example.labdata.entity.BrookfieldViscosityTemperaturePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 布鲁克菲尔德旋转黏度实验温度点存储库接口
 */
@Repository
public interface BrookfieldViscosityTemperaturePointRepository extends JpaRepository<BrookfieldViscosityTemperaturePoint, Long> {
    
    /**
     * 根据测试ID查找所有温度点
     */
    List<BrookfieldViscosityTemperaturePoint> findByTestId(Long testId);
    
    /**
     * 根据测试ID和点ID查找温度点
     */
    BrookfieldViscosityTemperaturePoint findByTestIdAndPointId(Long testId, String pointId);
}
