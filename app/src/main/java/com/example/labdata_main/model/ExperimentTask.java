package com.example.labdata_main.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.labdata_main.database.Converters;

import java.util.List;
import java.util.Map;

@Entity(tableName = "experiment_tasks")
public class ExperimentTask {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String taskId; // 格式：TASK_yyyyMMdd_HHmmss_序号
    private String taskName; // 任务名称
    private long projectId;
    private String projectName;
    private long creationTime;
    private long deadline; // 截止日期
    
    @TypeConverters(Converters.class)
    private List<MixRatio> selectedMixRatios;
    
    @TypeConverters(Converters.class)
    private Map<Long, List<String>> experimentAssignments; // Map<配比ID, 实验类型列表>
    
    private String moldingMethod;
    private String notes;

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
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

    public Map<Long, List<String>> getExperimentAssignments() {
        return experimentAssignments;
    }

    public void setExperimentAssignments(Map<Long, List<String>> experimentAssignments) {
        this.experimentAssignments = experimentAssignments;
    }

    public String getMoldingMethod() {
        return moldingMethod;
    }

    public void setMoldingMethod(String moldingMethod) {
        this.moldingMethod = moldingMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
