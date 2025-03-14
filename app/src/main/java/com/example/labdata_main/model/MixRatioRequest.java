package com.example.labdata_main.model;

import java.util.List;

public class MixRatioRequest {
    private String mixName;
    private List<AsphaltComponentRequest> asphaltComponents;
    private List<SandComponentRequest> sandComponents;
    private List<StoneComponentRequest> stoneComponents;
    // 添加单位ID和用户ID字段
    private Long mixCompany;
    private Long createdBy;

    public String getMixName() {
        return mixName;
    }

    public void setMixName(String mixName) {
        this.mixName = mixName;
    }

    public List<AsphaltComponentRequest> getAsphaltComponents() {
        return asphaltComponents;
    }

    public void setAsphaltComponents(List<AsphaltComponentRequest> asphaltComponents) {
        this.asphaltComponents = asphaltComponents;
    }

    public List<SandComponentRequest> getSandComponents() {
        return sandComponents;
    }

    public void setSandComponents(List<SandComponentRequest> sandComponents) {
        this.sandComponents = sandComponents;
    }

    public List<StoneComponentRequest> getStoneComponents() {
        return stoneComponents;
    }

    public void setStoneComponents(List<StoneComponentRequest> stoneComponents) {
        this.stoneComponents = stoneComponents;
    }
    
    // 添加mixCompany的getter和setter方法
    public Long getMixCompany() {
        return mixCompany;
    }
    
    public void setMixCompany(Long mixCompany) {
        this.mixCompany = mixCompany;
    }
    
    // 添加createdBy的getter和setter方法
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public static class AsphaltComponentRequest {
        private Long asphaltId;
        private Double percentage;

        public Long getAsphaltId() {
            return asphaltId;
        }

        public void setAsphaltId(Long asphaltId) {
            this.asphaltId = asphaltId;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class SandComponentRequest {
        private Long sandId;
        private String gradation;
        private Double percentage;

        public Long getSandId() {
            return sandId;
        }

        public void setSandId(Long sandId) {
            this.sandId = sandId;
        }

        public String getGradation() {
            return gradation;
        }

        public void setGradation(String gradation) {
            this.gradation = gradation;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class StoneComponentRequest {
        private Long stoneId;
        private String gradation;
        private Double percentage;

        public Long getStoneId() {
            return stoneId;
        }

        public void setStoneId(Long stoneId) {
            this.stoneId = stoneId;
        }

        public String getGradation() {
            return gradation;
        }

        public void setGradation(String gradation) {
            this.gradation = gradation;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }
}
