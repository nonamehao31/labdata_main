package com.example.labdata_main.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.labdata_main.model.ExperimentData;

import java.util.List;

@Dao
public interface ExperimentDataDao {
    @Insert
    long insert(ExperimentData experimentData);

    @Query("SELECT * FROM experiment_data")
    LiveData<List<ExperimentData>> getAllExperiments();

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio")
    LiveData<List<ExperimentData>> getExperimentsByMixRatio(String mixRatio);

    @Query("SELECT * FROM experiment_data WHERE taskId = :taskId")
    List<ExperimentData> getExperimentDataByTaskId(long taskId);

    @Query("SELECT * FROM experiment_data WHERE experimentName = :experimentName")
    List<ExperimentData> getExperimentDataByName(String experimentName);

    @Query("SELECT * FROM experiment_data WHERE experimentName = :experimentName " +
            "AND input1Label = :inputLabel AND input1Value = :inputValue")
    List<ExperimentData> getExperimentDataByInput1(String experimentName, String inputLabel, double inputValue);

    @Query("SELECT * FROM experiment_data WHERE experimentName = :experimentName " +
            "AND input2Label = :inputLabel AND input2Value = :inputValue")
    List<ExperimentData> getExperimentDataByInput2(String experimentName, String inputLabel, double inputValue);

    @Query("SELECT * FROM experiment_data WHERE deviceManufacturer = :manufacturer AND deviceModel = :model")
    List<ExperimentData> getExperimentDataByDevice(String manufacturer, String model);
}
