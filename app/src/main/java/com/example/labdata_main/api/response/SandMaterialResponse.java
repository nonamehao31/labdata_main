package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 沙子原料响应DTO
 */
public class SandMaterialResponse {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("name")
    private String name;
    
    public SandMaterialResponse() {
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
}
