package com.example.labdata.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "specimens")
@NoArgsConstructor
@AllArgsConstructor
public class Specimen {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "mix_ratio_id", nullable = true)
    private Long mixRatioId;
    
    @Column(name = "mixing_temperature", nullable = false)
    private Float mixingTemperature;
    
    @Column(name = "mixing_speed", nullable = false)
    private Float mixingSpeed;
    
    @Column(name = "mixing_time")
    private Integer mixingTime;
    
    @Column(name = "compaction_method")
    private String compactionMethod;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "creation_time", nullable = false)
    private Long creationTime;
    
    @Column(name = "cut_shape")
    private String cutShape;
    
    @Column(name = "cut_count", columnDefinition = "integer default 1")
    private Integer cutCount = 1;
    
    @Column(name = "length", columnDefinition = "float default 0")
    private Float length = 0f;
    
    @Column(name = "width", columnDefinition = "float default 0")
    private Float width = 0f;
    
    @Column(name = "height", columnDefinition = "float default 0")
    private Float height = 0f;
    
    @Column(name = "radius", columnDefinition = "float default 0")
    private Float radius = 0f;
    
    @Column(name = "specimen_company")
    private Long specimenCompany;
    
    // 修改ManyToOne关系配置，确保使用相同的列名
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mix_ratio_id", referencedColumnName = "id", insertable = false, updatable = false)
    private MixRatio mixRatio;
    
    // 用于创建新实例的便捷方法
    public static Specimen createWithBasicInfo(Long mixRatioId, Float mixingTemperature, 
                                              Float mixingSpeed, Integer mixingTime, String compactionMethod, Long createdBy, Long specimenCompany) {
        Specimen specimen = new Specimen();
        specimen.setMixRatioId(mixRatioId);
        specimen.setMixingTemperature(mixingTemperature);
        specimen.setMixingSpeed(mixingSpeed);
        specimen.setMixingTime(mixingTime);
        specimen.setCompactionMethod(compactionMethod);
        specimen.setCreatedBy(createdBy);
        specimen.setSpecimenCompany(specimenCompany);
        specimen.setCreationTime(Instant.now().toEpochMilli());
        specimen.setCutShape("rectangle"); // 默认值
        return specimen;
    }
}
