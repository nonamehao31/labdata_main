package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青原料响应DTO
 */
public class AsphaltMaterialResponse {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("grade")
    private String grade;
    
    @SerializedName("character")
    private String character;
    
    public AsphaltMaterialResponse() {
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
