package com.example.labdata_main.api.response;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青材料响应DTO
 */
public class TestAsphaltMaterialResponse {
    
    @SerializedName("id")
    private Long id;
    
    @SerializedName("asphaltSupplier")
    private String asphaltSupplier;
    
    @SerializedName("asphaltTestDue")
    private String asphaltTestDue;
    
    @SerializedName("asphaltGrade")
    private String asphaltGrade;
    
    @SerializedName("asphaltCatalog")
    private String asphaltCatalog;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("organizationId")
    private Long organizationId;
    
    @SerializedName("createdBy")
    private Long createdBy;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getAsphaltSupplier() {
        return asphaltSupplier;
    }
    
    public void setAsphaltSupplier(String asphaltSupplier) {
        this.asphaltSupplier = asphaltSupplier;
    }
    
    public String getAsphaltTestDue() {
        return asphaltTestDue;
    }
    
    public void setAsphaltTestDue(String asphaltTestDue) {
        this.asphaltTestDue = asphaltTestDue;
    }
    
    public String getAsphaltGrade() {
        return asphaltGrade;
    }
    
    public void setAsphaltGrade(String asphaltGrade) {
        this.asphaltGrade = asphaltGrade;
    }
    
    public String getAsphaltCatalog() {
        return asphaltCatalog;
    }
    
    public void setAsphaltCatalog(String asphaltCatalog) {
        this.asphaltCatalog = asphaltCatalog;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Long getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}
