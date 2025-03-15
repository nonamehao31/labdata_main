package com.example.labdata_main.api.request;

import com.google.gson.annotations.SerializedName;

/**
 * 沥青材料请求DTO
 */
public class TestAsphaltMaterialRequest {
    
    @SerializedName("asphaltSupplier")
    private String asphaltSupplier;
    
    @SerializedName("asphaltTestDue")
    private String asphaltTestDue;
    
    @SerializedName("asphaltGrade")
    private String asphaltGrade;
    
    @SerializedName("asphaltCatalog")
    private String asphaltCatalog;
    
    @SerializedName("projectId")
    private Long projectId;
    
    @SerializedName("remarks")
    private String remarks;
    
    public TestAsphaltMaterialRequest() {
    }
    
    public TestAsphaltMaterialRequest(String asphaltSupplier, String asphaltTestDue, String asphaltGrade, String asphaltCatalog) {
        this.asphaltSupplier = asphaltSupplier;
        this.asphaltTestDue = asphaltTestDue;
        this.asphaltGrade = asphaltGrade;
        this.asphaltCatalog = asphaltCatalog;
    }
    
    // Getters and Setters
    
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
    
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
