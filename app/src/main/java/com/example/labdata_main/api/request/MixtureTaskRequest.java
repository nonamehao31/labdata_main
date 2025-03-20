package com.example.labdata_main.api.request;

import java.util.List;
import java.util.Map;

/**
 * 混合料任务保存请求
 */
public class MixtureTaskRequest {
    
    private String projectId;
    private String taskName; 
    private String remarks;
    private List<MixratioSpecimenPair> mixratioSpecimenPairs;
    private List<String> taskAssignments;
    
    /**
     * 配比ID到实验类型的映射
     * key: 配比ID
     * value: 该配比ID对应的实验类型列表
     * 用于精确控制每个配比ID分配哪些实验，避免创建错误的组合
     */
    private Map<Long, List<String>> mixratioAssignments;
    
    /**
     * 配比与制件方式的组合
     */
    public static class MixratioSpecimenPair {
        private Long mixratioId;
        /**
         * specimenId字段实际上是存储制件方法ID (moldingMethodId)
         * 由于历史原因，数据库表使用specimen_id字段名
         */
        private Long specimenId;
        
        public MixratioSpecimenPair() {}
        
        public MixratioSpecimenPair(Long mixratioId, Long specimenId) {
            this.mixratioId = mixratioId;
            this.specimenId = specimenId;
        }
        
        public Long getMixratioId() {
            return mixratioId;
        }
        
        public void setMixratioId(Long mixratioId) {
            this.mixratioId = mixratioId;
        }
        
        public Long getSpecimenId() {
            return specimenId;
        }
        
        public void setSpecimenId(Long specimenId) {
            this.specimenId = specimenId;
        }
    }
    
    // Constructors
    public MixtureTaskRequest() {}
    
    public MixtureTaskRequest(String projectId, String taskName, String remarks, 
                             List<MixratioSpecimenPair> mixratioSpecimenPairs, 
                             List<String> taskAssignments) {
        this.projectId = projectId;
        this.taskName = taskName;
        this.remarks = remarks;
        this.mixratioSpecimenPairs = mixratioSpecimenPairs;
        this.taskAssignments = taskAssignments;
    }
    
    public MixtureTaskRequest(String projectId, String taskName, String remarks, 
                             List<MixratioSpecimenPair> mixratioSpecimenPairs, 
                             List<String> taskAssignments,
                             Map<Long, List<String>> mixratioAssignments) {
        this.projectId = projectId;
        this.taskName = taskName;
        this.remarks = remarks;
        this.mixratioSpecimenPairs = mixratioSpecimenPairs;
        this.taskAssignments = taskAssignments;
        this.mixratioAssignments = mixratioAssignments;
    }
    
    // Getters and Setters
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public List<MixratioSpecimenPair> getMixratioSpecimenPairs() {
        return mixratioSpecimenPairs;
    }
    
    public void setMixratioSpecimenPairs(List<MixratioSpecimenPair> mixratioSpecimenPairs) {
        this.mixratioSpecimenPairs = mixratioSpecimenPairs;
    }
    
    public List<String> getTaskAssignments() {
        return taskAssignments;
    }
    
    public void setTaskAssignments(List<String> taskAssignments) {
        this.taskAssignments = taskAssignments;
    }
    
    public Map<Long, List<String>> getMixratioAssignments() {
        return mixratioAssignments;
    }
    
    public void setMixratioAssignments(Map<Long, List<String>> mixratioAssignments) {
        this.mixratioAssignments = mixratioAssignments;
    }
}
