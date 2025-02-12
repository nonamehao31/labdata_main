package com.example.labdata_main.dao;

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
    ExperimentTask getTaskById(int id);

    @Query("SELECT * FROM experiment_tasks WHERE id = :taskId")
    ExperimentTask getExperimentTaskById(long taskId);

    @Query("SELECT * FROM experiment_tasks WHERE taskId = :taskId")
    ExperimentTask getTaskByTaskId(String taskId);

    @Query("SELECT * FROM experiment_tasks WHERE taskId LIKE :prefix || '%'")
    List<ExperimentTask> getTasksByPrefix(String prefix);

    @Query("SELECT COUNT(*) FROM experiment_tasks WHERE taskId LIKE :prefix || '%'")
    int getTaskCountByPrefix(String prefix);

    @Query("SELECT * FROM experiment_tasks WHERE companyId = :companyId AND task_status = '已完成' ORDER BY creationTime DESC")
    List<ExperimentTask> getCompletedTasksByCompany(String companyId);

    @Query("SELECT * FROM experiment_tasks WHERE companyId = :companyId AND (task_status IS NULL OR task_status != '已完成') ORDER BY creationTime DESC")
    List<ExperimentTask> getTasksByCompany(String companyId);

    @Query("SELECT * FROM experiment_tasks WHERE task_status != '已完成' ORDER BY creationTime DESC")
    List<ExperimentTask> getAllExperimentTasks();

    @Query("SELECT * FROM experiment_tasks")
    List<ExperimentTask> getTasksByType();

    @Query("SELECT * FROM experiment_tasks WHERE companyId = :companyId AND task_status = '已完成' " +
           "AND experiment_completion_time BETWEEN :startTime AND :endTime " +
           "ORDER BY experiment_completion_time DESC")
    List<ExperimentTask> getCompletedTasksByTimeRange(String companyId, long startTime, long endTime);

    @Query("SELECT * FROM experiment_tasks WHERE taskName = :taskName LIMIT 1")
    ExperimentTask getTaskByName(String taskName);
}
