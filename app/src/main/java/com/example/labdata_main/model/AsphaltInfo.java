package com.example.labdata_main.model;

import java.util.Objects;

public class AsphaltInfo {
    private Long id;
    private String supplier;
    private String expiryDate;
    private String grade;
    private String type;

    public AsphaltInfo(String supplier, String expiryDate, String grade, String type) {
        this.supplier = supplier;
        this.expiryDate = expiryDate;
        this.grade = grade;
        this.type = type;
    }
    
    public AsphaltInfo(Long id, String grade, String type, String supplier, String expiryDate) {
        this.id = id;
        this.grade = grade;
        this.type = type;
        this.supplier = supplier;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AsphaltInfo that = (AsphaltInfo) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(grade, that.grade) &&
               Objects.equals(type, that.type) &&
               Objects.equals(supplier, that.supplier) &&
               Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(grade, type, supplier, expiryDate);
    }
}
