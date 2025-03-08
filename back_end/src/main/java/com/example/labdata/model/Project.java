package com.example.labdata.model;

import com.example.labdata.model.audit.UserDateAudit;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project extends UserDateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Size(max = 500)
    private String description;
    
    // u9879u76eeu8d1fu8d23u4eba
    private String manager;
    
    // u9879u76eeu5730u70b9
    private String location;
    
    // u9879u76eeu5f00u59cbu65e5u671f
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    // u9879u76eeu8ba1u5212u7ed3u675fu65e5u671f
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    // u9879u76eeu72b6u6001: u8fdbu884cu4e2du3001u5df2u5b8cu6210u3001u5df2u6682u505cu7b49
    private String status;
    
    // u540cu6b65u6807u8bb0u5b57u6bb5
    private Long clientId;
    private Long organizationId; // u9650u5236u4ec5u663eu793au7528u6237u6240u5728u5355u4f4du7684u6570u636e
    private boolean synced = false;
}
