package com.example.labdata.payload.response;

import java.util.List;
import java.util.Map;

/**
 * 沥青实验任务详情响应类
 * 包含沥青信息和实验指派信息
 */
public class AsphaltDetailResponse {
    
    private List<AsphaltInfo> asphaltInfoList;
    private Map<Long, List<String>> experimentAssignments;
    
    public AsphaltDetailResponse() {
    }
    
    public AsphaltDetailResponse(List<AsphaltInfo> asphaltInfoList, Map<Long, List<String>> experimentAssignments) {
        this.asphaltInfoList = asphaltInfoList;
        this.experimentAssignments = experimentAssignments;
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
        
        public AsphaltInfo(Long asphaltId, String asphaltSupplier, String asphaltGrade, String asphaltCatalog) {
            this.asphaltId = asphaltId;
            this.asphaltSupplier = asphaltSupplier;
            this.asphaltGrade = asphaltGrade;
            this.asphaltCatalog = asphaltCatalog;
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
