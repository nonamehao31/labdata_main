package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.labdata_main.model.AsphaltExperimentData;

import java.util.List;
import java.util.Map;

@Dao
public interface AsphaltExperimentDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(AsphaltExperimentData data);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<AsphaltExperimentData> dataList);

    @Update
    void update(AsphaltExperimentData data);

    @Delete
    void delete(AsphaltExperimentData data);

    @Query("DELETE FROM asphalt_experiment_data WHERE taskId = :taskId")
    void deleteByTaskId(long taskId);

    @Query("SELECT * FROM asphalt_experiment_data WHERE id = :id")
    AsphaltExperimentData getById(long id);

    @Query("SELECT * FROM asphalt_experiment_data WHERE taskId = :taskId")
    List<AsphaltExperimentData> getByTaskId(long taskId);

    @Query("SELECT * FROM asphalt_experiment_data WHERE taskId = :taskId AND experimentType = :experimentType")
    List<AsphaltExperimentData> getByTaskIdAndType(long taskId, String experimentType);

    @Query("SELECT * FROM asphalt_experiment_data WHERE taskId = :taskId AND deviceCode = :deviceCode")
    List<AsphaltExperimentData> getByTaskIdAndDevice(long taskId, String deviceCode);

    @Query("SELECT DISTINCT experimentType FROM asphalt_experiment_data WHERE taskId = :taskId")
    List<String> getExperimentTypesByTaskId(long taskId);

    @Query("SELECT COUNT(*) FROM asphalt_experiment_data WHERE taskId = :taskId")
    int getExperimentCountByTaskId(long taskId);

    @Transaction
    @Query("SELECT * FROM asphalt_experiment_data WHERE taskId = :taskId ORDER BY experimentType")
    List<AsphaltExperimentData> getAllExperimentDataForTask(long taskId);

    @Query("SELECT * FROM asphalt_experiment_data WHERE createTime BETWEEN :startTime AND :endTime")
    List<AsphaltExperimentData> getExperimentDataByTimeRange(long startTime, long endTime);

    @Query("SELECT * FROM asphalt_experiment_data WHERE taskId IN (:taskIds)")
    List<AsphaltExperimentData> getExperimentDataByTaskIds(List<Long> taskIds);

    // 统计相关查询
    @Query("SELECT COUNT(*) FROM asphalt_experiment_data WHERE experimentType = :experimentType")
    int getExperimentCountByType(String experimentType);

    @Query("SELECT experimentType, COUNT(*) as count FROM asphalt_experiment_data GROUP BY experimentType")
    List<ExperimentTypeCount> getExperimentTypeCounts();

    class ExperimentTypeCount {
        public String experimentType;
        public int count;
    }

    // 批量操作
    @Transaction
    default void updateExperimentDataBatch(List<AsphaltExperimentData> dataList) {
        for (AsphaltExperimentData data : dataList) {
            update(data);
        }
    }

    @Query("DELETE FROM asphalt_experiment_data WHERE taskId IN (:taskIds)")
    void deleteByTaskIds(List<Long> taskIds);

    // 数据验证
    @Query("SELECT COUNT(*) > 0 FROM asphalt_experiment_data WHERE taskId = :taskId AND experimentType = :experimentType")
    boolean hasExperimentData(long taskId, String experimentType);

    @Query("SELECT COUNT(*) FROM asphalt_experiment_data WHERE taskId = :taskId AND experimentType = :experimentType AND deviceCode IS NULL")
    int countMissingDeviceCodes(long taskId, String experimentType);
}
