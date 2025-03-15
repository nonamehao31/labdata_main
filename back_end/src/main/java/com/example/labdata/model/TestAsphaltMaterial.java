package com.example.labdata.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 沥青材料实体类
 */
@Entity
@Table(name = "test_asphalt_material")
@Data
@NoArgsConstructor
public class TestAsphaltMaterial {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asphalt_supplier", nullable = false)
    private String asphaltSupplier;
    
    @Column(name = "asphalt_test_due", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate asphaltTestDue;
    
    @Column(name = "asphalt_grade", nullable = false)
    private String asphaltGrade;
    
    @Column(name = "asphalt_catalog", nullable = false)
    private String asphaltCatalog;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // 创建前设置时间戳
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 更新前更新时间戳
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
