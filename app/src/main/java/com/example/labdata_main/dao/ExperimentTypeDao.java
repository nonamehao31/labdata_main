package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.labdata_main.model.ExperimentType;
import java.util.List;

@Dao
public interface ExperimentTypeDao {
    @Query("SELECT * FROM experiment_types WHERE category = :category")
    List<ExperimentType> getExperimentTypesByCategory(String category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<ExperimentType> experimentTypes);

    @Query("SELECT COUNT(*) FROM experiment_types")
    int getCount();

    @Query("SELECT * FROM experiment_types WHERE type = :type LIMIT 1")
    ExperimentType findByType(String type);

    @Query("SELECT * FROM experiment_types WHERE name = :name AND category = :category")
    List<ExperimentType> getExperimentTypesByNameAndCategory(String name, String category);
}
