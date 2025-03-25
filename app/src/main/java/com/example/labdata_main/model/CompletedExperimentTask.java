package com.example.labdata_main.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 已完成实验任务统一模型
 */
public class CompletedExperimentTask {
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
        result.setTaskAssignment("试件ID: " + (task.getSpecimenId() != null ? task.getSpecimenId() : "未知"));
        
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
}
