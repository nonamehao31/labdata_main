package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.labdata_main.model.MaterialProperty;

import java.util.List;

@Dao
public interface MaterialPropertyDao {
    @Insert
    void insert(MaterialProperty property);

    @Query("SELECT * FROM material_properties WHERE type = :type")
    List<MaterialProperty> getPropertiesByType(String type);

    @Query("DELETE FROM material_properties WHERE type = :type")
    void deletePropertiesByType(String type);

    @Query("SELECT * FROM material_properties")
    List<MaterialProperty> getAllMaterialProperties();
}
