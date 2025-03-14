package com.example.labdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sand_material")
public class SandMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sand_id")
    private Long id;

    @NotBlank(message = "沙子名称不能为空")
    @Column(name = "sand_name")
    private String name;
    
    @NotBlank(message = "公司ID不能为空")
    @Column(name = "sand_company")
    private String company;
}
