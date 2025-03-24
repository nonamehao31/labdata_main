package com.example.labdata.repository;

import com.example.labdata.entity.DsrTemperaturePoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DsrTemperaturePointRepository extends JpaRepository<DsrTemperaturePoint, Long> {
    List<DsrTemperaturePoint> findByTestId(Long testId);
}