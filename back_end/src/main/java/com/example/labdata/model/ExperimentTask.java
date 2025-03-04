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
    
    public enum TaskStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}

