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

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio")
    List<ExperimentData> getExperimentDataByMixRatio(String mixRatio);

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio")
    LiveData<List<ExperimentData>> getAllExperimentDataByMixRatio(String mixRatio);

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio AND (" +
           "(:paramType = 'stability' AND experimentName LIKE '%稳定度%') OR " +
           "(:paramType = 'flow' AND experimentName LIKE '%流值%') OR " +
           "(:paramType = 'dynamic_stability' AND experimentName LIKE '%动稳定度%') OR " +
           "(:paramType = 'penetration' AND experimentName LIKE '%针入度%') OR " +
           "(:paramType = 'softening_point' AND experimentName LIKE '%软化点%') OR " +
           "(:paramType = 'ductility' AND experimentName LIKE '%延度%'))")
    LiveData<List<ExperimentData>> getExperimentDataByMixRatioAndParamType(String mixRatio, String paramType);

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio AND " +
           "CASE :mainExperimentType " +
           "WHEN '马歇尔实验' THEN experimentName LIKE '%马歇尔%' " +
           "WHEN '车辙实验' THEN experimentName LIKE '%车辙%' " +
           "WHEN '沥青实验' THEN (experimentName LIKE '%针入度%' OR experimentName LIKE '%软化点%' OR experimentName LIKE '%延度%') " +
           "ELSE 0 END")
    List<ExperimentData> getExperimentDataByMixRatioAndMainExperimentType(String mixRatio, String mainExperimentType);

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio AND " +
           "CASE :mainExperimentType " +
           "WHEN '马歇尔实验' THEN experimentName LIKE '%马歇尔%' " +
           "WHEN '车辙实验' THEN experimentName LIKE '%车辙%' " +
           "WHEN '沥青实验' THEN (experimentName LIKE '%针入度%' OR experimentName LIKE '%软化点%' OR experimentName LIKE '%延度%') " +
           "ELSE 0 END " +
           "AND CASE :subExperimentType " +
           "WHEN 'stability' THEN experimentName LIKE '%稳定度%' " +
           "WHEN 'flow' THEN experimentName LIKE '%流值%' " +
           "WHEN 'dynamic_stability' THEN experimentName LIKE '%动稳定度%' " +
           "WHEN 'penetration' THEN experimentName LIKE '%针入度%' " +
           "WHEN 'softening_point' THEN experimentName LIKE '%软化点%' " +
           "WHEN 'ductility' THEN experimentName LIKE '%延度%' " +
           "ELSE 0 END")
    LiveData<List<ExperimentData>> getExperimentDataByMixRatioAndMainExperimentTypeAndSubExperimentType(
        String mixRatio, 
        String mainExperimentType,
        String subExperimentType
    );

    @Query("SELECT * FROM experiment_data WHERE " +
           "experimentName LIKE '%_stability_%' AND " +
           "mixRatio = :mixRatio")
    LiveData<List<ExperimentData>> getMarshallStabilityData(String mixRatio);

    @Query("SELECT * FROM experiment_data WHERE " +
           "experimentName LIKE '%_flow_%' AND " +
           "mixRatio = :mixRatio")
    LiveData<List<ExperimentData>> getMarshallFlowData(String mixRatio);

    @Query("SELECT * FROM experiment_data WHERE " +
           "experimentName LIKE '%_' || :dataType || '_%' AND " +
           "mixRatio = :mixRatio")
    LiveData<List<ExperimentData>> getMarshallDataByType(String mixRatio, String dataType);

    @Query("SELECT * FROM experiment_data WHERE mixRatio = :mixRatio AND " +
           "(experimentName LIKE '%车辙%' OR experimentName LIKE '%rutting%') AND " +
           "(experimentName LIKE '%_first_slope%' OR " +
           "experimentName LIKE '%_first_intercept%' OR " +
           "experimentName LIKE '%_second_slope%' OR " +
           "experimentName LIKE '%_second_intercept%')")
    LiveData<List<ExperimentData>> getRuttingDataByType(String mixRatio);
}
