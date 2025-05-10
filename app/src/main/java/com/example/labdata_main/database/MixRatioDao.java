package com.example.labdata_main.database;

import androidx.lifecycle.LiveData;
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
    void insert(MixRatio mixRatio);

    @Update
    void update(MixRatio mixRatio);

    @Delete
    void delete(MixRatio mixRatio);

    @Query("SELECT * FROM mix_ratios")
    List<MixRatio> getAllMixRatios();

    @Query("SELECT * FROM mix_ratios WHERE project_id = :projectId")
    LiveData<List<MixRatio>> getMixRatiosForProject(long projectId);

    @Query("SELECT * FROM mix_ratios WHERE id = :mixRatioId")
    LiveData<MixRatio> getMixRatioById(long mixRatioId);
}