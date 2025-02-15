package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.labdata_main.model.ExperimentDataField;
import java.util.List;

@Dao
public interface ExperimentDataFieldDao {
    @Query("SELECT * FROM experiment_data_fields WHERE experiment_type_id = :experimentTypeId")
    List<ExperimentDataField> getFieldsByExperimentTypeId(long experimentTypeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ExperimentDataField> fields);

    @Query("SELECT COUNT(*) FROM experiment_data_fields")
    int getCount();
}
