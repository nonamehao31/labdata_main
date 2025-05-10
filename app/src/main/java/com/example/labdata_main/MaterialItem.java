package com.example.labdata_main;

import java.io.Serializable;

public class MaterialItem implements Serializable {
    private String name;
    private float percentage;
    private String type;

    public MaterialItem(String name, float percentage, String type) {
        this.name = name;
        this.percentage = percentage;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPercentage() {
        return percentage;
    }

    public void setPercentage(float percentage) {
        this.percentage = percentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
