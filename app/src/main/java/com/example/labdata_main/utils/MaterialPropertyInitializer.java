package com.example.labdata_main.utils;

import android.content.Context;
import android.util.Log;

import com.example.labdata_main.model.MaterialProperty;
import com.example.labdata_main.database.DatabaseHelper;

public class MaterialPropertyInitializer {
    private static final String TAG = "MaterialPropertyInitializer";

    public static void initializeMaterialProperties(Context context) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
        
        // 删除现有的属性
        databaseHelper.deleteMaterialPropertiesByType("asphalt");
        databaseHelper.deleteMaterialPropertiesByType("sand");
        databaseHelper.deleteMaterialPropertiesByType("stone");

        // 沥青材料属性
        MaterialProperty asphalt1 = new MaterialProperty();
        asphalt1.setType("asphalt");
        asphalt1.setName("中石化");
        asphalt1.setCode("c1");
        databaseHelper.insertMaterialProperty(asphalt1);

        MaterialProperty asphalt2 = new MaterialProperty();
        asphalt2.setType("asphalt");
        asphalt2.setName("海湾石油");
        asphalt2.setCode("h3");
        databaseHelper.insertMaterialProperty(asphalt2);

        // 沙子材料属性
        MaterialProperty sand = new MaterialProperty();
        sand.setType("sand");
        sand.setName("标准砂");
        sand.setCode("std1");
        databaseHelper.insertMaterialProperty(sand);

        // 石子材料属性
        MaterialProperty stone1 = new MaterialProperty();
        stone1.setType("stone");
        stone1.setName("玄武岩");
        stone1.setCode("basalt1");
        databaseHelper.insertMaterialProperty(stone1);

        MaterialProperty stone2 = new MaterialProperty();
        stone2.setType("stone");
        stone2.setName("石灰岩");
        stone2.setCode("limestone1");
        databaseHelper.insertMaterialProperty(stone2);

        Log.d(TAG, "Material properties initialized");
    }
}
