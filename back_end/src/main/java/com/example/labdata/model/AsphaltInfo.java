package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "asphalt_info")
@Data
@NoArgsConstructor
public class AsphaltInfo extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String grade;  // 沥青标号
    
    @NotBlank
    @Size(max = 100)
    private String type;   // 沥青类型
    
    @NotBlank
    @Size(max = 200)
    private String supplier;  // 供应商
    
    private LocalDate expiryDate;  // 检测截止日期
    
    // 添加与实验任务的关联
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "experiment_task_id")
    private ExperimentTask experimentTask;
    
    // 同步字段
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
}
