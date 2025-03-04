package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "specimens")
@Data
@NoArgsConstructor
public class Specimen extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;  // QR Code or unique identifier
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mix_ratio_id")
    private MixRatio mixRatio;
    
    private Double mixingTemperature;
    private Double mixingSpeed;
    private String compactionMethod;
    private Instant creationTime;
    
    @Enumerated(EnumType.STRING)
    private CutShape cutShape;
    
    private Integer cutCount;
    private Double length;
    private Double width;
    private Double height;
    private Double radius;
    
    // Sync fields
    private Long clientId;
    private boolean synced = false;
    private String syncStatus = "NEW";
    
    public enum CutShape {
        CYLINDER, RECTANGULAR, BEAM, OTHER
    }
}

