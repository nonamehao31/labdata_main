package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.MixRatio;

import java.util.List;

@Dao
public interface MixRatioDao {
    @Insert
    long insert(MixRatio mixRatio);

    @Update
    void update(MixRatio mixRatio);

    @Delete
    void delete(MixRatio mixRatio);

    @Query("SELECT * FROM mix_ratios ORDER BY id DESC")
    List<MixRatio> getAllMixRatios();

    @Query("SELECT * FROM mix_ratios WHERE id = :id")
    MixRatio getMixRatioById(long id);
}
