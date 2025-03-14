package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "material_properties")
public class MaterialProperty {
    @PrimaryKey(autoGenerate = true)
    private int id;
    public String type;
    public String name;
    public String unit;
    public String code;
    public String grade;
    public String character;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCharacter() {
        return character;
    }

    public void setCharacter(String character) {
        this.character = character;
    }
}
