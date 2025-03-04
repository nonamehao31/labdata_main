package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "experiment_types")
@Data
@NoArgsConstructor
public class ExperimentType extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "experiment_type_fields", joinColumns = @JoinColumn(name = "experiment_type_id"))
    private List<ExperimentField> fields = new ArrayList<>();
    
    // Sync fields
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class ExperimentField {
        private String name;
        private String dataType; // STRING, NUMBER, DATE, BOOLEAN
        private String unit;
        private boolean required;
        
        public ExperimentField(String name, String dataType, String unit, boolean required) {
            this.name = name;
            this.dataType = dataType;
            this.unit = unit;
            this.required = required;
        }
    }
}

