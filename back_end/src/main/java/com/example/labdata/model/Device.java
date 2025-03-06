package com.example.labdata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Device {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String manufacturer;
    
    @Column(nullable = false)
    private String model;
    
    @Column(name = "purchase_year", nullable = false)
    private String purchaseYear;
    
    @Column(name = "company_id", nullable = false)
    private String companyId;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
