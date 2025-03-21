package com.example.labdata.model;

import com.example.labdata.utils.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

/**
 * 沥青混合料弯曲试验数据模型
 */
@Entity
@Table(name = "asphalt_mixture_bending_test")
@Data
public class MixtureBendingTest {

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
    @Column(name = "mix_ratio_id", nullable = false)
    private Long mixRatioId;

    /**
     * 跨径长度L (mm)
     */
    @Column(name = "span_length")
    private Float spanLength;

    /**
     * 试件数量
     */
    @Column(name = "specimen_count", nullable = false)
    private Integer specimenCount;

    /**
     * 平均抗弯拉强度 (MPa)
     */
    @Column(name = "average_flexural_strength")
    private Float averageFlexuralStrength;

    /**
     * 平均最大弯拉应变 (με)
     */
    @Column(name = "average_max_strain")
    private Float averageMaxStrain;

    /**
     * 平均弯曲劲度模量 (MPa)
     */
    @Column(name = "average_stiffness_modulus")
    private Float averageStiffnessModulus;

    /**
     * 试件数据JSON
     * 存储格式为数组，每个元素包含一个试件的所有数据
     */
    @Column(name = "specimens", columnDefinition = "jsonb")
    @Type(JsonBinaryType.class)
    private String specimens;

    /**
     * 记录创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 数据保存前的操作
     */
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    /**
     * 数据更新前的操作
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
