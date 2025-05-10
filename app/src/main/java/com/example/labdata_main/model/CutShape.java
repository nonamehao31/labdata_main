package com.example.labdata_main.model;

public enum CutShape {
    CUBOID("长方体"),
    CYLINDER("圆柱体"),
    HALF_CYLINDER("半圆柱体");

    private final String displayName;

    CutShape(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
