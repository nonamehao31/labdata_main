package com.example.labdata_main.api.request;

import java.util.List;

/**
 * 混合料任务保存请求
 */
public class MixtureTaskRequest {
    
    private Long projectId;
    private String remarks;
    private List<MixratioSpecimenPair> mixratioSpecimenPairs;
    private List<String> taskAssignments;
    
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
    
    public MixtureTaskRequest(Long projectId, String remarks, 
                             List<MixratioSpecimenPair> mixratioSpecimenPairs, 
                             List<String> taskAssignments) {
        this.projectId = projectId;
        this.remarks = remarks;
        this.mixratioSpecimenPairs = mixratioSpecimenPairs;
        this.taskAssignments = taskAssignments;
    }
    
    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }
    
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
}
