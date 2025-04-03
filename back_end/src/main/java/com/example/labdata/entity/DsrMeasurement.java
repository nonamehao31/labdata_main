package com.example.labdata.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 动态剪切流变仪测量值实体类
 */
@Entity
@Table(name = "dsr_measurement")
@Data
@NoArgsConstructor
public class DsrMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long temperaturePointId;

    private String taskId;

    private Double loadFrequency;

    private Double maxShearStress;

    private Double maxShearStrain;

    private Double phaseAngle;

    private Double complexShearModulus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}