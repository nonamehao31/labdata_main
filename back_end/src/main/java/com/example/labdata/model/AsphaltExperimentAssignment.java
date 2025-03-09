package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "asphalt_experiment_assignments")
@Data
@NoArgsConstructor
public class AsphaltExperimentAssignment extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asphalt_info_id", nullable = false)
    private AsphaltInfo asphaltInfo;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experiment_type_id", nullable = false)
    private ExperimentType experimentType;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experiment_task_id", nullable = false)
    private ExperimentTask experimentTask;
    
    // 同步字段
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
}
