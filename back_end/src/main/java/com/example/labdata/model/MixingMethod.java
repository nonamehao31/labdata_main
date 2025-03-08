package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mixing_methods")
@Data
@NoArgsConstructor
public class MixingMethod extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 1000)
    private String description;
    
    // 混合步骤，存储为JSON字符串
    @Column(columnDefinition = "TEXT")
    private String steps;
    
    // 混合温度 (°C)
    private Double mixingTemperature;
    
    // 混合时间 (分钟)
    private Double mixingTime;
    
    // 同步标记字段
    private Long clientId;
    private Long organizationId; // 限制仅显示用户所在单位的数据
    private boolean synced = false;
}
