package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * 石子原料请求DTO
 */
public class StoneMaterialRequest {
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("company")
    private String companyId;
    
    public StoneMaterialRequest() {
    }
    
    public StoneMaterialRequest(String name) {
        this.name = name;
    }
    
    public StoneMaterialRequest(String name, String companyId) {
        this.name = name;
        this.companyId = companyId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
