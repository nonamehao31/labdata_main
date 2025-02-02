package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.labdata_main.database.Converters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "experiment_tasks")
public class ExperimentTask implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String taskId; // 格式：TASK_yyyyMMdd_HHmmss_序号
    private String taskName; // 任务名称
    private long projectId;
    private String projectName;
    private long creationTime;
    private long deadline; // 截止日期
    private String status; // 任务状态：未接受、已接受等
    @TypeConverters(Converters.class)
    private List<MixRatio> selectedMixRatios;
    @TypeConverters(Converters.class)
    private Map<Long, List<String>> experimentAssignments; // Map<配比ID, 实验类型列表>
    private String moldingMethod;
    private String notes;

    public ExperimentTask() {
        selectedMixRatios = new ArrayList<>();
        experimentAssignments = new HashMap<>();
    }

    protected ExperimentTask(Parcel in) {
        id = in.readLong();
        taskId = in.readString();
        taskName = in.readString();
        projectId = in.readLong();
        projectName = in.readString();
        creationTime = in.readLong();
        deadline = in.readLong();
        status = in.readString();
        moldingMethod = in.readString();
        notes = in.readString();

        // 使用Gson来序列化和反序列化复杂对象
        Gson gson = new Gson();
        
        // 反序列化selectedMixRatios
        String mixRatiosJson = in.readString();
        Type mixRatiosType = new TypeToken<List<MixRatio>>(){}.getType();
        selectedMixRatios = gson.fromJson(mixRatiosJson, mixRatiosType);

        // 反序列化experimentAssignments
        String assignmentsJson = in.readString();
        Type assignmentsType = new TypeToken<Map<Long, List<String>>>(){}.getType();
        experimentAssignments = gson.fromJson(assignmentsJson, assignmentsType);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeLong(projectId);
        dest.writeString(projectName);
        dest.writeLong(creationTime);
        dest.writeLong(deadline);
        dest.writeString(status);
        dest.writeString(moldingMethod);
        dest.writeString(notes);

        // 使用Gson来序列化复杂对象
        Gson gson = new Gson();
        dest.writeString(gson.toJson(selectedMixRatios));
        dest.writeString(gson.toJson(experimentAssignments));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ExperimentTask> CREATOR = new Creator<ExperimentTask>() {
        @Override
        public ExperimentTask createFromParcel(Parcel in) {
            return new ExperimentTask(in);
        }

        @Override
        public ExperimentTask[] newArray(int size) {
            return new ExperimentTask[size];
        }
    };

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
