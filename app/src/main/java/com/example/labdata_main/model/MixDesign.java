package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mix_designs")
public class MixDesign {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int designGroup;
    public String materialType;
    public String materialName;
    public String materialCode;
    public double proportion;
    public String gradation;
    public double percentage;
    public long timestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDesignGroup() {
        return designGroup;
    }

    public void setDesignGroup(int designGroup) {
        this.designGroup = designGroup;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public double getProportion() {
        return proportion;
    }

    public void setProportion(double proportion) {
        this.proportion = proportion;
    }

    public String getGradation() {
        return gradation;
    }

    public void setGradation(String gradation) {
        this.gradation = gradation;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
