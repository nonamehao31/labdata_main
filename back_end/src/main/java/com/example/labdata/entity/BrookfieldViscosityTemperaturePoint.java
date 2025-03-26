package com.example.labdata.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * 布鲁克菲尔德旋转黏度实验温度点实体类
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brookfield_viscosity_temperature_point")
public class BrookfieldViscosityTemperaturePoint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "point_id", nullable = false)
    private String pointId;
    
    @Column(name = "temperature", nullable = false)
    private String temperature;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    @JsonBackReference("test-temperature-points")
    private BrookfieldViscosityTest test;
    
    @OneToMany(mappedBy = "temperaturePoint", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("temperature-point-measurements")
    private Set<BrookfieldViscosityMeasurement> measurements = new HashSet<>();
    
    // 添加和移除粘度测量值的辅助方法
    public void addMeasurement(BrookfieldViscosityMeasurement measurement) {
        measurements.add(measurement);
        measurement.setTemperaturePoint(this);
    }
    
    public void removeMeasurement(BrookfieldViscosityMeasurement measurement) {
        measurements.remove(measurement);
        measurement.setTemperaturePoint(null);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
