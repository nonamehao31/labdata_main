package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.MixDesign;

import java.util.List;

@Dao
public interface MixDesignDao {
    @Insert
    long insert(MixDesign design);

    @Update
    void update(MixDesign design);

    @Delete
    void delete(MixDesign design);

    @Query("SELECT * FROM mix_designs")
    List<MixDesign> getAllMixDesigns();

    @Query("SELECT MAX(designGroup) FROM mix_designs")
    int getLatestDesignGroup();

    @Query("SELECT * FROM mix_designs WHERE designGroup = :groupId")
    List<MixDesign> getMixDesignsByGroup(int groupId);
}
