package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experiment_data")
@Data
@NoArgsConstructor
public class ExperimentData extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "experiment_task_id")
    private ExperimentTask experimentTask;
    
    @ElementCollection
    @CollectionTable(name = "experiment_data_values", joinColumns = @JoinColumn(name = "experiment_data_id"))
    private List<ExperimentDataValue> dataValues = new ArrayList<>();
    
    private String notes;
    
    // Sync fields
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class ExperimentDataValue {
        private String fieldName;
        private String value;
        
        public ExperimentDataValue(String fieldName, String value) {
            this.fieldName = fieldName;
            this.value = value;
        }
    }
}

