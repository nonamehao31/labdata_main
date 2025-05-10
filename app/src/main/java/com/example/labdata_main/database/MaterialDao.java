package com.example.labdata_main.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.labdata_main.model.Material;
import java.util.List;

@Dao
public interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY name ASC")
    List<Material> getAllMaterials();

    @Query("SELECT * FROM materials WHERE id = :id")
    Material getMaterialById(String id);

    @Query("SELECT * FROM materials WHERE category = :category")
    List<Material> getMaterialsByCategory(String category);

    @Insert
    void insert(Material material);

    @Update
    void update(Material material);

    @Delete
    void delete(Material material);
}
