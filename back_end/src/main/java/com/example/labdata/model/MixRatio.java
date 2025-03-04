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
@Table(name = "mix_ratios")
@Data
@NoArgsConstructor
public class MixRatio extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "mix_ratio_materials",
        joinColumns = @JoinColumn(name = "mix_ratio_id"),
        inverseJoinColumns = @JoinColumn(name = "material_id")
    )
    private List<Material> materials = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "material_percentages", joinColumns = @JoinColumn(name = "mix_ratio_id"))
    private List<MaterialPercentage> materialPercentages = new ArrayList<>();
    
    // Sync fields
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class MaterialPercentage {
        private Long materialId;
        private Double percentage;
        
        public MaterialPercentage(Long materialId, Double percentage) {
            this.materialId = materialId;
            this.percentage = percentage;
        }
    }
}

