package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experiment_tasks")
@Data
@NoArgsConstructor
public class ExperimentTask extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experiment_type_id")
    private ExperimentType experimentType;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specimen_id")
    private Specimen specimen;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
    
    private Instant scheduledStartTime;
    private Instant scheduledEndTime;
    private Instant actualStartTime;
    private Instant actualEndTime;
    
    @Enumerated(EnumType.STRING)
    private TaskStatus status;
    
    // Sync fields
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    // 添加关联字段
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mixing_method_id")
    private MixingMethod mixingMethod;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mix_ratio_id")
    private MixRatio mixRatio;
    
    // 可能需要一个多对多关系来表示多个材料
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "experiment_task_materials",
        joinColumns = @JoinColumn(name = "experiment_task_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();
    
    private Long organizationId; // 确保组织隔离
    
    // 新增字段
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private ExperimentCategory category; // MIXTURE, ASPHALT

    @OneToMany(mappedBy = "experimentTask", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsphaltInfo> asphaltInfos = new ArrayList<>();

    public enum TaskStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // 添加实验类别枚举
    public enum ExperimentCategory {
        MIXTURE, ASPHALT
    }
}
