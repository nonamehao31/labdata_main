package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 制件方法实体类
 */
@Entity
@Table(name = "compaction_methods")
@Data
@NoArgsConstructor
public class CompactionMethod {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 单位/组织名称
     */
    @Column(name = "organization", nullable = false)
    private String organization;
    
    /**
     * 制件方法名称
     */
    @Column(name = "method_name", nullable = false)
    private String methodName;
    
    /**
     * 拌合温度(°C)
     */
    @Column(name = "mixing_temperature")
    private Float mixingTemperature;
    
    /**
     * 拌合速度(rpm)
     */
    @Column(name = "mixing_speed")
    private Float mixingSpeed;
    
    /**
     * 拌合时间(min)
     */
    @Column(name = "mixing_time")
    private Float mixingTime;
    
    /**
     * 压实方法描述
     */
    @Column(name = "compaction_method", nullable = false)
    private String compactionMethod;
    
    /**
     * 试件类型（立方体、圆柱体等）
     */
    @Column(name = "specimen_type")
    private Integer specimenType;
    
    /**
     * 是否是默认方法
     */
    @Column(name = "is_default", columnDefinition = "boolean default false")
    private Boolean isDefault = false;
    
    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 创建时间
     */
    @Column(name = "creation_time")
    private Long creationTime;
}
