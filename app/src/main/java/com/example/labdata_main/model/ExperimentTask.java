package com.example.labdata_main.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.ColumnInfo;
import com.example.labdata_main.database.Converters;

import java.util.List;
import java.util.Map;

@Entity(tableName = "experiment_tasks")
public class ExperimentTask {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "task_id")
    private String taskId;

    @ColumnInfo(name = "project_id")
    private long projectId;

    @ColumnInfo(name = "project_name")
    private String projectName;
    
    @ColumnInfo(name = "creation_time")
    private long creationTime;
    
    @ColumnInfo(name = "deadline")
    private long deadline;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "selected_mix_ratios")
    private List<MixRatio> selectedMixRatios;
    
    @ColumnInfo(name = "molding_method")
    private String moldingMethod;
    
    @TypeConverters(Converters.class)
    @ColumnInfo(name = "experiment_assignments")
    private Map<Long, List<String>> experimentAssignments;
    
    @ColumnInfo(name = "notes")
    private String notes;

    // Getters and setters
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public List<MixRatio> getSelectedMixRatios() {
        return selectedMixRatios;
    }

    public void setSelectedMixRatios(List<MixRatio> selectedMixRatios) {
        this.selectedMixRatios = selectedMixRatios;
    }

    public String getMoldingMethod() {
        return moldingMethod;
    }

    public void setMoldingMethod(String moldingMethod) {
        this.moldingMethod = moldingMethod;
    }

    public Map<Long, List<String>> getExperimentAssignments() {
        return experimentAssignments;
    }

    public void setExperimentAssignments(Map<Long, List<String>> experimentAssignments) {
        this.experimentAssignments = experimentAssignments;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
