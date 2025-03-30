package com.example.labdata_main.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Keep;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 已完成实验任务统一模型
 */
@Keep
public class CompletedExperimentTask implements Serializable, Parcelable {
    // 添加序列化ID，确保序列化兼容性
    private static final long serialVersionUID = 1234567890123456789L;
    
    // 任务基本信息
    private String taskId;
    private String taskName;
    private String taskAssignment;
    private String experimenter;  // 实验人员
    private long acceptTime;      // 接受时间
    private long completionTime;  // 完成时间
    
    // 实验类型信息
    private boolean isMixtureTask;  // 是否为混合料任务
    private String experimentType;  // 实验类型
    private String experimentName;  // 实验名称
    
    // 混合料任务特有信息
    private String mixratioId;
    private String mixName;
    private String specimenId;
    private String compactionMethod;
    
    // 沥青任务特有信息
    private String asphaltExperimentId;
    private String asphaltExperimentType;
    
    // 实验设备信息
    private String assignedAsphaltEquipment;
    private String assignedAsphaltEquipmentManufacturer;
    
    // 无参构造函数
    public CompletedExperimentTask() {
    }
    
    // Parcelable 构造函数
    protected CompletedExperimentTask(Parcel in) {
        // 使用 ClassLoader 读取防止类加载器问题
        ClassLoader classLoader = CompletedExperimentTask.class.getClassLoader();
        
        taskId = in.readString();
        taskName = in.readString();
        taskAssignment = in.readString();
        experimenter = in.readString();
        acceptTime = in.readLong();
        completionTime = in.readLong();
        isMixtureTask = in.readByte() != 0;
        experimentType = in.readString();
        experimentName = in.readString();
        mixratioId = in.readString();
        mixName = in.readString();
        specimenId = in.readString();
        compactionMethod = in.readString();
        asphaltExperimentId = in.readString();
        asphaltExperimentType = in.readString();
        assignedAsphaltEquipment = in.readString();
        assignedAsphaltEquipmentManufacturer = in.readString();
    }
    
    // 实现 Parcelable.Creator
    @Keep
    public static final Creator<CompletedExperimentTask> CREATOR = new Creator<CompletedExperimentTask>() {
        @Override
        public CompletedExperimentTask createFromParcel(Parcel in) {
            return new CompletedExperimentTask(in);
        }
        
        @Override
        public CompletedExperimentTask[] newArray(int size) {
            return new CompletedExperimentTask[size];
        }
    };
    
    // 实现 Parcelable 接口的方法
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeString(taskAssignment);
        dest.writeString(experimenter);
        dest.writeLong(acceptTime);
        dest.writeLong(completionTime);
        dest.writeByte((byte) (isMixtureTask ? 1 : 0));
        dest.writeString(experimentType);
        dest.writeString(experimentName);
        dest.writeString(mixratioId);
        dest.writeString(mixName);
        dest.writeString(specimenId);
        dest.writeString(compactionMethod);
        dest.writeString(asphaltExperimentId);
        dest.writeString(asphaltExperimentType);
        dest.writeString(assignedAsphaltEquipment);
        dest.writeString(assignedAsphaltEquipmentManufacturer);
    }
    
    // 工具方法 - 格式化时间
    public static String formatTime(long timestamp) {
        if (timestamp <= 0) {
            return "未知";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    // Getters & Setters
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
    
    public String getTaskAssignment() {
        return taskAssignment;
    }
    
    public void setTaskAssignment(String taskAssignment) {
        this.taskAssignment = taskAssignment;
    }
    
    public String getExperimenter() {
        return experimenter;
    }
    
    public void setExperimenter(String experimenter) {
        this.experimenter = experimenter;
    }
    
    public long getAcceptTime() {
        return acceptTime;
    }
    
    public void setAcceptTime(long acceptTime) {
        this.acceptTime = acceptTime;
    }
    
    public long getCompletionTime() {
        return completionTime;
    }
    
    public void setCompletionTime(long completionTime) {
        this.completionTime = completionTime;
    }
    
    public boolean isMixtureTask() {
        return isMixtureTask;
    }
    
    public void setMixtureTask(boolean mixtureTask) {
        isMixtureTask = mixtureTask;
    }
    
    public String getExperimentType() {
        return experimentType;
    }
    
    public void setExperimentType(String experimentType) {
        this.experimentType = experimentType;
    }
    
    public String getExperimentName() {
        return experimentName;
    }
    
    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }
    
    public String getMixratioId() {
        return mixratioId;
    }
    
    public void setMixratioId(String mixratioId) {
        this.mixratioId = mixratioId;
    }
    
    public String getMixName() {
        return mixName;
    }
    
    public void setMixName(String mixName) {
        this.mixName = mixName;
    }
    
    public String getSpecimenId() {
        return specimenId;
    }
    
    public void setSpecimenId(String specimenId) {
        this.specimenId = specimenId;
    }
    
    public String getCompactionMethod() {
        return compactionMethod;
    }
    
    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }
    
    public String getAsphaltExperimentId() {
        return asphaltExperimentId;
    }
    
    public void setAsphaltExperimentId(String asphaltExperimentId) {
        this.asphaltExperimentId = asphaltExperimentId;
    }
    
    public String getAsphaltExperimentType() {
        return asphaltExperimentType;
    }
    
    public void setAsphaltExperimentType(String asphaltExperimentType) {
        this.asphaltExperimentType = asphaltExperimentType;
    }
    
    public String getAssignedAsphaltEquipment() {
        return assignedAsphaltEquipment;
    }
    
    public void setAssignedAsphaltEquipment(String assignedAsphaltEquipment) {
        this.assignedAsphaltEquipment = assignedAsphaltEquipment;
    }
    
    public String getAssignedAsphaltEquipmentManufacturer() {
        return assignedAsphaltEquipmentManufacturer;
    }
    
    public void setAssignedAsphaltEquipmentManufacturer(String assignedAsphaltEquipmentManufacturer) {
        this.assignedAsphaltEquipmentManufacturer = assignedAsphaltEquipmentManufacturer;
    }
    
    // 获取格式化的设备信息
    public String getFormattedEquipmentInfo() {
        if (assignedAsphaltEquipmentManufacturer != null && !assignedAsphaltEquipmentManufacturer.isEmpty() 
            && assignedAsphaltEquipment != null && !assignedAsphaltEquipment.isEmpty()) {
            return assignedAsphaltEquipmentManufacturer + " " + assignedAsphaltEquipment;
        } else if (assignedAsphaltEquipment != null && !assignedAsphaltEquipment.isEmpty()) {
            return assignedAsphaltEquipment;
        } else if (assignedAsphaltEquipmentManufacturer != null && !assignedAsphaltEquipmentManufacturer.isEmpty()) {
            return assignedAsphaltEquipmentManufacturer;
        } else {
            return "未知";
        }
    }
    
    /**
     * 获取任务ID
     * @return 任务ID
     */
    public Long getId() {
        if (taskId == null || taskId.isEmpty()) {
            return 0L;
        }
        try {
            return Long.parseLong(taskId);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    /**
     * 获取任务类型
     * @return 任务类型，MIXTURE表示混合料任务，ASPHALT表示沥青任务
     */
    public String getType() {
        return isMixtureTask ? "MIXTURE" : "ASPHALT";
    }
    
    // 工厂方法 - 从混合料任务创建
    public static CompletedExperimentTask fromMixtureTask(com.example.labdata_main.api.response.CompletedMixtureTaskResponse task) {
        CompletedExperimentTask result = new CompletedExperimentTask();
        result.setMixtureTask(true);
        result.setTaskId(task.getTaskId());
        result.setTaskName(task.getTaskName());
        result.setExperimenter(task.getAcceptor());
        result.setAcceptTime(task.getAcceptTime() != null ? parseTimeStringToLong(task.getAcceptTime()) : 0L);
        result.setCompletionTime(task.getCreationTime() != null ? parseTimeStringToLong(task.getCreationTime()) : 0L);
        
        // 设置混合料特有信息
        result.setMixratioId(task.getMixratioId());
        result.setMixName(task.getMixName());
        result.setSpecimenId(task.getSpecimenId());
        result.setCompactionMethod(task.getCompactionMethod());
        
        // 设置实验类型和名称
        result.setExperimentType("混合料实验");
        
        // 构建任务名称 - 如果有配比名称，则增加配比信息
        if (task.getMixName() != null && !task.getMixName().isEmpty()) {
            String mixNameWithDetails = task.getMixName();
            // 根据memory中记录的同名配比区分逻辑，添加ID和压实方法
            if (task.getMixratioId() != null && task.getCompactionMethod() != null) {
                mixNameWithDetails += " (ID:" + task.getMixratioId() + ", " + task.getCompactionMethod() + ")";
            }
            result.setExperimentName(task.getTaskName() + " - " + mixNameWithDetails);
        } else {
            result.setExperimentName(task.getTaskName());
        }
        
        // 设置任务指派信息
        if (task.getTaskAssignment() != null && !task.getTaskAssignment().isEmpty()) {
            // 优先使用任务中提供的指派信息
            result.setTaskAssignment(task.getTaskAssignment());
        } else {
            // 如果任务中没有指派信息，则使用试件ID作为默认指派信息
            result.setTaskAssignment("试件ID: " + (task.getSpecimenId() != null ? task.getSpecimenId() : "未知"));
        }
        
        return result;
    }
    
    // 工厂方法 - 从沥青任务创建
    public static CompletedExperimentTask fromAsphaltTask(com.example.labdata_main.api.response.CompletedAsphaltTaskResponse task) {
        CompletedExperimentTask result = new CompletedExperimentTask();
        result.setMixtureTask(false);
        result.setTaskId(task.getTaskId());
        result.setTaskName(task.getTaskName());
        result.setExperimenter(task.getAcceptor());
        result.setAcceptTime(task.getAcceptTime() != null ? parseTimeStringToLong(task.getAcceptTime()) : 0L);
        result.setCompletionTime(task.getUpdatedAt() != null ? parseTimeStringToLong(task.getUpdatedAt()) : 0L);
        
        // 设置沥青实验特有信息
        result.setAsphaltExperimentId(task.getExperimentId());
        result.setAsphaltExperimentType(task.getExperimentType());
        
        // 设置实验类型和名称
        result.setExperimentType("沥青实验");
        result.setExperimentName(task.getExperimentName() != null ? task.getExperimentName() : task.getTaskName());
        
        // 设置任务指派信息
        result.setTaskAssignment(task.getTaskAssignment() != null ? task.getTaskAssignment() : "无指派信息");
        
        return result;
    }

    /**
     * 将ISO 8601格式的时间字符串解析为长整型时间戳
     * @param timeString ISO 8601格式的时间字符串，如 "2025-03-25T04:29:24.031+00:00"
     * @return 长整型时间戳，解析失败时返回0
     */
    private static long parseTimeStringToLong(String timeString) {
        try {
            // 如果时间字符串是数字形式，直接解析
            if (timeString.matches("\\d+")) {
                return Long.parseLong(timeString);
            }
            
            // 否则尝试解析ISO 8601格式的时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            Date date = sdf.parse(timeString);
            return date != null ? date.getTime() : 0L;
        } catch (Exception e) {
            // 解析失败，返回0
            return 0L;
        }
    }

    /**
     * 获取实验名称中的项目名部分
     * 从格式"实验名称-配比名称（ID: XX,压实方法）"中提取"实验名称"部分
     * @return 只包含项目名的字符串
     */
    public String getProjectName() {
        if (experimentName == null || experimentName.isEmpty()) {
            return "";
        }
        
        // 检查字符串中是否包含连字符"-"
        int dashIndex = experimentName.indexOf('-');
        if (dashIndex > 0) {
            // 提取连字符前的部分作为项目名
            return experimentName.substring(0, dashIndex).trim();
        }
        
        // 如果没有连字符，则返回整个实验名
        return experimentName;
    }
}
