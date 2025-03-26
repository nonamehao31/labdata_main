package com.example.labdata.entity;

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
 * 布鲁克菲尔德旋转黏度实验实体类
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brookfield_viscosity_test")
public class BrookfieldViscosityTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private String taskId;
    
    @Column(name = "experimenter")
    private String experimenter;
    
    @Column(name = "test_date")
    private Long testDate;
    
    @Column(name = "device_id")
    private String deviceId;
    
    @Column(name = "device_name")
    private String deviceName;
    
    @Column(name = "device_manufacturer")
    private String deviceManufacturer;
    
    @Column(name = "device_model")
    private String deviceModel;
    
    @Column(name = "created_at")
    private Instant createdAt;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference("test-temperature-points")
    private Set<BrookfieldViscosityTemperaturePoint> temperaturePoints = new HashSet<>();
    
    // 添加和移除温度点的辅助方法
    public void addTemperaturePoint(BrookfieldViscosityTemperaturePoint temperaturePoint) {
        temperaturePoints.add(temperaturePoint);
        temperaturePoint.setTest(this);
    }
    
    public void removeTemperaturePoint(BrookfieldViscosityTemperaturePoint temperaturePoint) {
        temperaturePoints.remove(temperaturePoint);
        temperaturePoint.setTest(null);
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
