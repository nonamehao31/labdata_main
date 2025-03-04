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
@Table(name = "materials")
@Data
@NoArgsConstructor
public class Material extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    private String type;
    
    private Double density;
    
    @ElementCollection
    @CollectionTable(name = "material_properties", joinColumns = @JoinColumn(name = "material_id"))
    private List<MaterialProperty> properties = new ArrayList<>();
    
    // 添加同步相关字段
    private Long clientId; // 客户端生成的ID
    private boolean synced = false;
    private String syncStatus = "NEW"; // NEW, SYNCED, MODIFIED, CONFLICT
}

