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
    
    // 总量 (如kg或g)
    private String totalAmount;
    
    // 项目ID
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    
    // 关联混合方法
    @ManyToOne
    @JoinColumn(name = "mixing_method_id")
    private MixingMethod mixingMethod;
    
    // 同步标记字段
    private Long clientId;
    private Long organizationId; // 限制仅显示用户所在单位的数据
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    @Embeddable
    @Data
    @NoArgsConstructor
    public static class MaterialPercentage {
        private Long materialId;
        private String materialName; // 冗余存储，方便查询
        private Double percentage;
        
        public MaterialPercentage(Long materialId, Double percentage) {
            this.materialId = materialId;
            this.percentage = percentage;
        }
    }
}
