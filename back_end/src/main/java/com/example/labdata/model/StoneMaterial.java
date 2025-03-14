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
@Table(name = "stone_material")
public class StoneMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stone_id")
    private Long id;

    @NotBlank(message = "石子名称不能为空")
    @Column(name = "stone_name")
    private String name;
    
    @NotBlank(message = "公司ID不能为空")
    @Column(name = "stone_company")
    private String company;
}
