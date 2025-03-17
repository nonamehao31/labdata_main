package com.example.labdata_main.api.model;

import java.util.List;
import java.util.Map;

/**
 * 沥青实验任务详情响应模型
 * 对应后端的AsphaltDetailResponse
 */
public class AsphaltDetailResponse {
    
    private List<AsphaltInfo> asphaltInfoList;
    private Map<Long, List<String>> experimentAssignments;
    
    public AsphaltDetailResponse() {
    }
    
    public List<AsphaltInfo> getAsphaltInfoList() {
        return asphaltInfoList;
    }
    
    public void setAsphaltInfoList(List<AsphaltInfo> asphaltInfoList) {
        this.asphaltInfoList = asphaltInfoList;
    }
    
    public Map<Long, List<String>> getExperimentAssignments() {
        return experimentAssignments;
    }
    
    public void setExperimentAssignments(Map<Long, List<String>> experimentAssignments) {
        this.experimentAssignments = experimentAssignments;
    }
    
    /**
     * 沥青信息内部类
     */
    public static class AsphaltInfo {
        private Long asphaltId;
        private String asphaltSupplier;
        private String asphaltGrade;
        private String asphaltCatalog;
        
        public AsphaltInfo() {
        }
        
        public Long getAsphaltId() {
            return asphaltId;
        }
        
        public void setAsphaltId(Long asphaltId) {
            this.asphaltId = asphaltId;
        }
        
        public String getAsphaltSupplier() {
            return asphaltSupplier;
        }
        
        public void setAsphaltSupplier(String asphaltSupplier) {
            this.asphaltSupplier = asphaltSupplier;
        }
        
        public String getAsphaltGrade() {
            return asphaltGrade;
        }
        
        public void setAsphaltGrade(String asphaltGrade) {
            this.asphaltGrade = asphaltGrade;
        }
        
        public String getAsphaltCatalog() {
            return asphaltCatalog;
        }
        
        public void setAsphaltCatalog(String asphaltCatalog) {
            this.asphaltCatalog = asphaltCatalog;
        }
    }
}
