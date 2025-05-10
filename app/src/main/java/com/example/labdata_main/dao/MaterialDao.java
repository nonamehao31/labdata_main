package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.Material;
import com.example.labdata_main.model.MixDesign;

import java.util.List;

@Dao
public interface MaterialDao {
    @Insert
    long insert(Material material);

    @Update
    void update(Material material);

    @Delete
    void delete(Material material);

    @Query("SELECT * FROM materials")
    List<Material> getAllMaterials();

    @Query("SELECT * FROM materials WHERE category = :category")
    List<Material> getMaterialsByCategory(String category);

    // MixDesign 相关方法
    @Insert
    void insertMixDesign(MixDesign design);

    @Query("SELECT MAX(designGroup) FROM mix_designs")
    int getLatestDesignGroup();

    @Query("SELECT * FROM mix_designs WHERE designGroup = :groupId")
    List<MixDesign> getMixDesignsByGroup(int groupId);
}
