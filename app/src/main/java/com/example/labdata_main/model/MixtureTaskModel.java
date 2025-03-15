package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MixtureTaskModel implements Serializable {
    @SerializedName("taskId")
    private Long id;
    
    @SerializedName("taskName")
    private String name;
    
    @SerializedName("taskType")
    private String type;
    
    public MixtureTaskModel() {}
    
    public MixtureTaskModel(Long id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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
        
        MixtureTaskModel that = (MixtureTaskModel) o;
        
        return id != null ? id.equals(that.id) : that.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
