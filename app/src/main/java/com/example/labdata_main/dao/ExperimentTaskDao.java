package com.example.labdata_main.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.ExperimentTask;

import java.util.List;

@Dao
public interface ExperimentTaskDao {
    @Insert
    long insert(ExperimentTask task);

    @Update
    void update(ExperimentTask task);

    @Delete
    void delete(ExperimentTask task);

    @Query("SELECT * FROM experiment_tasks WHERE id = :id")
    ExperimentTask getTaskById(long id);

    @Query("SELECT * FROM experiment_tasks WHERE taskId = :taskId")
    ExperimentTask getTaskByTaskId(String taskId);

    @Query("SELECT * FROM experiment_tasks WHERE projectId = :projectId")
    List<ExperimentTask> getTasksByProject(long projectId);

    @Query("SELECT * FROM experiment_tasks ORDER BY creationTime DESC")
    LiveData<List<ExperimentTask>> getAllTasks();

    @Query("SELECT COUNT(*) FROM experiment_tasks WHERE taskId LIKE :prefix || '%'")
    int getTaskCountByPrefix(String prefix);
}
