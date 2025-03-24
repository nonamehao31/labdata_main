package com.example.labdata.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 沥青弯曲蠕变劲度试验（弯曲梁流变仪法）实体类
 */
@Entity
@Table(name = "bbr_test")
@Data
@NoArgsConstructor
public class BbrTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 基本信息
    private String taskId;                 // 任务ID
    private String operatorId;             // 操作员ID
    private LocalDateTime testDate;        // 测试日期
    private String specimenId;             // 试件ID
    private String specimenType;           // 试件类型
    private String materialType;           // 材料类型
    private String remarks;                // 备注
    
    // 试件尺寸数据
    private Double beamSpan;               // 弯曲梁跨度(mm)
    private Double specimenWidth;          // 试件宽度(mm)
    private Double specimenHeight;         // 试件高度(mm)
    
    // 8秒时间点数据
    private Double temperature8s;          // 8秒时的实验温度(°C)
    private Double load8s;                 // 8秒时的施加荷载(N)
    private Double deflection8s;           // 8秒时的变形挠度(mm)
    private Double stiffness8s;            // 8秒时的弯曲蠕变劲度模量(MPa)
    
    // 15秒时间点数据
    private Double temperature15s;         // 15秒时的实验温度(°C)
    private Double load15s;                // 15秒时的施加荷载(N)
    private Double deflection15s;          // 15秒时的变形挠度(mm)
    private Double stiffness15s;           // 15秒时的弯曲蠕变劲度模量(MPa)
    
    // 30秒时间点数据
    private Double temperature30s;         // 30秒时的实验温度(°C)
    private Double load30s;                // 30秒时的施加荷载(N)
    private Double deflection30s;          // 30秒时的变形挠度(mm)
    private Double stiffness30s;           // 30秒时的弯曲蠕变劲度模量(MPa)
    
    // 60秒时间点数据
    private Double temperature60s;         // 60秒时的实验温度(°C)
    private Double load60s;                // 60秒时的施加荷载(N)
    private Double deflection60s;          // 60秒时的变形挠度(mm)
    private Double stiffness60s;           // 60秒时的弯曲蠕变劲度模量(MPa)
    
    // 120秒时间点数据
    private Double temperature120s;        // 120秒时的实验温度(°C)
    private Double load120s;               // 120秒时的施加荷载(N)
    private Double deflection120s;         // 120秒时的变形挠度(mm)
    private Double stiffness120s;          // 120秒时的弯曲蠕变劲度模量(MPa)
    
    // 240秒时间点数据
    private Double temperature240s;        // 240秒时的实验温度(°C)
    private Double load240s;               // 240秒时的施加荷载(N)
    private Double deflection240s;         // 240秒时的变形挠度(mm)
    private Double stiffness240s;          // 240秒时的弯曲蠕变劲度模量(MPa)
    
    // 计算结果
    private Double creepRate;              // 蠕变速率(m值)
    
    // 时间戳
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
