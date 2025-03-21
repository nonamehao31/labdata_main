package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 汉堡车辙实验数据模型
 */
@Entity
@Table(name = "hamburg_rutting_test")
@Data
public class HamburgRuttingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的任务ID
     */
    @Column(name = "task_id", nullable = false)
    private String taskId;

    /**
     * 关联的配比ID
     */
    @Column(name = "mix_ratio_id")
    private Long mixRatioId;

    /**
     * 第一稳态曲线斜率
     */
    @Column(name = "steady_slope1")
    private Float steadySlope1;

    /**
     * 第一稳态曲线截距
     */
    @Column(name = "steady_curvilinear1")
    private Float steadyCurvilinear1;

    /**
     * 第二稳态曲线斜率
     */
    @Column(name = "steady_slope2")
    private Float steadySlope2;

    /**
     * 第二稳态曲线截距
     */
    @Column(name = "steady_curvilinear2")
    private Float steadyCurvilinear2;

    /**
     * 记录创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 记录更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 数据保存前的操作
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 数据更新前的操作
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
