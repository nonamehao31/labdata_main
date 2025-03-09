package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;
import com.example.labdata_main.database.Converters;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Entity(tableName = "experiment_tasks")
public class ExperimentTask implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "taskId")
    private String taskId; // 格式：TASK_yyyyMMdd_HHmmss_序号
    private String taskName; // 任务名称
    private long projectId;
    private String projectName;
    private String companyId; // 公司ID
    @ColumnInfo(name = "experimenter")
    private String experimenter; // 实验人员
    private long creationTime;
    @ColumnInfo(name = "deadline")
    private long deadline; // 截止日期
    @ColumnInfo(name = "preparation_time")
    private long preparationTime;
    @ColumnInfo(name = "specimen_generation_time")
    private long specimenGenerationTime;
    @ColumnInfo(name = "experiment_completion_time")
    private long experimentCompletionTime;
    @ColumnInfo(name = "task_status")
    private String status; // 任务状态：未接受、已接受等
    @TypeConverters(Converters.class)
    private List<MixRatio> selectedMixRatios;
    @TypeConverters(Converters.class)
    private Map<Long, List<String>> experimentAssignments; // Map<配比ID, 实验类型列表>
    @TypeConverters(Converters.class)
    private String moldingMethod;
    @TypeConverters(Converters.class)
    private List<String> selectedMixingDevices;  // 选择的搅拌设备
    @TypeConverters(Converters.class)
    private List<String> selectedFormingDevices;  // 选择的成型设备
    private String notes;
    private String experimentType; // 新增实验类型字段：MIXTURE 或 ASPHALT
    
    // 添加用于后端同步的字段
    private Long mixingMethodId; // 制件方法ID
    private Long mixRatioId;     // 配比ID
    @TypeConverters(Converters.class)
    private List<Long> materialIds; // 原料ID列表

    public ExperimentTask() {
        selectedMixRatios = new ArrayList<>();
        experimentAssignments = new HashMap<>();
        selectedMixingDevices = new ArrayList<>();
        selectedFormingDevices = new ArrayList<>();
        materialIds = new ArrayList<>();
        // 为taskId设置一个默认值
        SimpleDateFormat taskIdFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        taskId = "TASK_" + taskIdFormat.format(new Date()) + "_0";
    }

    protected ExperimentTask(Parcel in) {
        id = in.readLong();
        taskId = in.readString();
        taskName = in.readString();
        projectId = in.readLong();
        projectName = in.readString();
        companyId = in.readString();
        experimenter = in.readString();
        creationTime = in.readLong();
        deadline = in.readLong();
        preparationTime = in.readLong();
        specimenGenerationTime = in.readLong();
        experimentCompletionTime = in.readLong();
        status = in.readString();
        moldingMethod = in.readString();
        notes = in.readString();
        experimentType = in.readString();

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

        // 反序列化设备信息
        String mixingDevicesJson = in.readString();
        Type mixingDevicesType = new TypeToken<List<String>>(){}.getType();
        selectedMixingDevices = gson.fromJson(mixingDevicesJson, mixingDevicesType);

        String formingDevicesJson = in.readString();
        Type formingDevicesType = new TypeToken<List<String>>(){}.getType();
        selectedFormingDevices = gson.fromJson(formingDevicesJson, formingDevicesType);

        // 读取同步字段
        mixingMethodId = in.readLong();
        mixRatioId = in.readLong();
        String materialIdsJson = in.readString();
        Type materialIdsType = new TypeToken<List<Long>>(){}.getType();
        materialIds = gson.fromJson(materialIdsJson, materialIdsType);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeLong(projectId);
        dest.writeString(projectName);
        dest.writeString(companyId);
        dest.writeString(experimenter);
        dest.writeLong(creationTime);
        dest.writeLong(deadline);
        dest.writeLong(preparationTime);
        dest.writeLong(specimenGenerationTime);
        dest.writeLong(experimentCompletionTime);
        dest.writeString(status);
        dest.writeString(moldingMethod);
        dest.writeString(notes);
        dest.writeString(experimentType);

        // 使用Gson来序列化复杂对象
        Gson gson = new Gson();
        dest.writeString(gson.toJson(selectedMixRatios));
        dest.writeString(gson.toJson(experimentAssignments));
        dest.writeString(gson.toJson(selectedMixingDevices));
        dest.writeString(gson.toJson(selectedFormingDevices));

        // 写入同步字段
        dest.writeLong(mixingMethodId != null ? mixingMethodId : 0L);
        dest.writeLong(mixRatioId != null ? mixRatioId : 0L);
        dest.writeString(gson.toJson(materialIds));
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

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getExperimenter() {
        return experimenter;
    }

    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
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

    public long getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(long preparationTime) {
        this.preparationTime = preparationTime;
    }

    public long getSpecimenGenerationTime() {
        return specimenGenerationTime;
    }

    public void setSpecimenGenerationTime(long specimenGenerationTime) {
        this.specimenGenerationTime = specimenGenerationTime;
    }

    public long getExperimentCompletionTime() {
        return experimentCompletionTime;
    }

    public void setExperimentCompletionTime(long experimentCompletionTime) {
        this.experimentCompletionTime = experimentCompletionTime;
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

    public List<String> getSelectedMixingDevices() {
        return selectedMixingDevices != null ? selectedMixingDevices : new ArrayList<>();
    }

    public void setSelectedMixingDevices(List<String> selectedMixingDevices) {
        this.selectedMixingDevices = selectedMixingDevices;
    }

    public List<String> getSelectedFormingDevices() {
        return selectedFormingDevices != null ? selectedFormingDevices : new ArrayList<>();
    }

    public void setSelectedFormingDevices(List<String> selectedFormingDevices) {
        this.selectedFormingDevices = selectedFormingDevices;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }

    public Long getMixingMethodId() {
        return mixingMethodId;
    }

    public void setMixingMethodId(Long mixingMethodId) {
        this.mixingMethodId = mixingMethodId;
    }

    public Long getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(Long mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public List<Long> getMaterialIds() {
        return materialIds != null ? materialIds : new ArrayList<>();
    }

    public void setMaterialIds(List<Long> materialIds) {
        this.materialIds = materialIds;
    }
}
