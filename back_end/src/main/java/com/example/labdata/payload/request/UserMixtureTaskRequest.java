package com.example.labdata.payload.request;

import lombok.Data;

import java.util.List;

/**
 * 用户混合料任务请求DTO
 */
@Data
public class UserMixtureTaskRequest {
    
    /**
     * 项目ID
     */
    private Long projectId;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 配比与制件方式组合
     */
    private List<MixratioSpecimenPair> mixratioSpecimenPairs;
    
    /**
     * 任务指派
     */
    private List<String> taskAssignments;
    
    /**
     * 配比与制件方式的组合
     */
    @Data
    public static class MixratioSpecimenPair {
        /**
         * 配比ID
         */
        private Long mixratioId;
        
        /**
         * 制件方式ID
         */
        private Long specimenId;
    }
}
