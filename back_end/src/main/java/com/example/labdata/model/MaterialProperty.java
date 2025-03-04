package com.example.labdata.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Embeddable
@Data
@NoArgsConstructor
public class MaterialProperty {
    @NotBlank
    @Size(max = 100)
    private String name;
    
    private String value;
    
    private String unit;
    
    public MaterialProperty(String name, String value, String unit) {
        this.name = name;
        this.value = value;
        this.unit = unit;
    }
}

