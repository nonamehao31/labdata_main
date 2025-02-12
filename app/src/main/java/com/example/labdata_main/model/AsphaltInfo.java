package com.example.labdata_main.model;

public class AsphaltInfo {
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
}
