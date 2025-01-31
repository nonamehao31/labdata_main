package com.example.labdata_main.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.ExperimentTask;

import java.util.List;

@Dao
public interface ExperimentTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ExperimentTask task);

    @Update
    void update(ExperimentTask task);

    @Delete
    void delete(ExperimentTask task);

    @Query("SELECT * FROM experiment_tasks WHERE task_id = :taskId")
    ExperimentTask getTaskById(String taskId);

    @Query("SELECT * FROM experiment_tasks WHERE project_id = :projectId ORDER BY creation_time DESC")
    List<ExperimentTask> getTasksByProject(long projectId);

    @Query("SELECT * FROM experiment_tasks ORDER BY creation_time DESC")
    LiveData<List<ExperimentTask>> getAllTasksLiveData();

    @Query("SELECT * FROM experiment_tasks ORDER BY creation_time DESC")
    List<ExperimentTask> getAllTasks();

    @Query("SELECT COUNT(*) FROM experiment_tasks WHERE task_id LIKE :prefix || '%'")
    int getTaskCountByPrefix(String prefix);
}
