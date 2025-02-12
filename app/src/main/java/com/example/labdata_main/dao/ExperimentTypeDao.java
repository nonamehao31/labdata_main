package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.labdata_main.model.ExperimentType;
import java.util.List;

@Dao
public interface ExperimentTypeDao {
    @Query("SELECT * FROM experiment_types WHERE type = :type")
    List<ExperimentType> getExperimentTypesByCategory(String type);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExperimentType> experimentTypes);

    @Query("SELECT COUNT(*) FROM experiment_types")
    int getCount();
}
