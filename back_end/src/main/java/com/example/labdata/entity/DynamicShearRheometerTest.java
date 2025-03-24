package com.example.labdata.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 动态剪切流变仪实验实体类
 */
@Entity
@Table(name = "dynamic_shear_rheometer_test")
@Data
@NoArgsConstructor
public class DynamicShearRheometerTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String taskId;

    @Column(length = 255)
    private String operatorId;

    @Column(length = 255)
    private String specimenId;

    @Column(length = 255)
    private String specimenType;

    @Column(length = 255)
    private String materialType;

    @Column(length = 255)
    private String controlMode;

    private Double testRadius;

    private Double plateGap;

    @Column(length = 1000)
    private String remarks;

    @Column(columnDefinition = "TEXT")
    private String experimentValues;

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