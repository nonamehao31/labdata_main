package com.example.labdata.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * 布鲁克菲尔德旋转黏度实验粘度测量值实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brookfield_viscosity_measurement")
public class BrookfieldViscosityMeasurement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "measurement_id", nullable = false)
    private String measurementId;
    
    @Column(name = "spindle_type")
    private String spindleType;
    
    @Column(name = "rotation_speed")
    private String rotationSpeed;
    
    @Column(name = "viscosity", nullable = false)
    private String viscosity;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "temperature_point_id", nullable = false)
    private BrookfieldViscosityTemperaturePoint temperaturePoint;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
