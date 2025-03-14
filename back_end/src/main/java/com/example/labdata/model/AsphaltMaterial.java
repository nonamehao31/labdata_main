package com.example.labdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "asphalt_material")
public class AsphaltMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asphalt_id")
    private Long id;

    @NotBlank(message = "沥青名称不能为空")
    @Column(name = "asphalt_name")
    private String name;

    @NotBlank(message = "沥青标号不能为空")
    @Column(name = "asphalt_grade")
    private String grade;

    @NotBlank(message = "沥青性质不能为空")
    @Pattern(regexp = "NORMAL|MODIFIED", message = "沥青性质只能是NORMAL或MODIFIED")
    @Column(name = "asphalt_character")
    private String character;
    
    @NotBlank(message = "公司ID不能为空")
    @Column(name = "asphalt_company")
    private String company;
}
