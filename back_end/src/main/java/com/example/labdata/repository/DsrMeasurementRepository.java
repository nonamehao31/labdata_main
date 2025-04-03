package com.example.labdata.repository;

import com.example.labdata.entity.DsrMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DsrMeasurementRepository extends JpaRepository<DsrMeasurement, Long> {
    List<DsrMeasurement> findByTemperaturePointId(Long temperaturePointId);
    
    // 添加基于taskId的查询方法
    List<DsrMeasurement> findByTaskId(String taskId);
}