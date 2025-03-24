package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 延度实验实体类
 */
@Data
@Entity
@Table(name = "ductility_test")
public class DuctilityTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private String taskId; // 使用String类型处理任务ID，避免大数值问题

    @Column(name = "temperature", nullable = false)
    private String temperature; // 温度

    @Column(name = "displacement", nullable = false)
    private String displacement; // 拉长位移

    @Column(name = "experimenter")
    private String experimenter; // 实验人员

    @Column(name = "test_date")
    private Long testDate; // 实验日期

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "device_id")
    private String deviceId; // 设备ID

    @Column(name = "device_name")
    private String deviceName; // 设备名称

    @Column(name = "device_manufacturer")
    private String deviceManufacturer; // 设备制造商

    @Column(name = "device_model")
    private String deviceModel; // 设备型号

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
