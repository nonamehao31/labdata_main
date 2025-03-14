package com.example.labdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mixratio")
public class MixRatio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank(message = "配合比名称不能为空")
    @Column(name = "mix_name")
    private String mixName;

    @Column(name = "mix_id", unique = true)
    private String mixId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "mix_company")
    private Long mixCompany;
    
    @Column(name = "created_by")
    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
