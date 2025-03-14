package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青原料请求DTO
 */
public class AsphaltMaterialRequest {
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("grade")
    private String grade;
    
    @SerializedName("character")
    private String character;
    
    @SerializedName("company")
    private String companyId;
    
    public AsphaltMaterialRequest() {
    }
    
    public AsphaltMaterialRequest(String name, String grade, String character, String companyId) {
        this.name = name;
        this.grade = grade;
        this.character = character;
        this.companyId = companyId;
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
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
