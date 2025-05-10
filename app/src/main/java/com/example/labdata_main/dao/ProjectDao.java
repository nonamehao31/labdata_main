package com.example.labdata_main.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.labdata_main.model.Project;

import java.util.List;

@Dao
public interface ProjectDao {
    @Insert
    long insert(Project project);

    @Update
    void update(Project project);

    @Delete
    void delete(Project project);

    @Query("SELECT * FROM projects ORDER BY id DESC")
    List<Project> getAllProjects();

    @Query("SELECT * FROM projects WHERE id = :id")
    Project getProjectById(long id);
}
