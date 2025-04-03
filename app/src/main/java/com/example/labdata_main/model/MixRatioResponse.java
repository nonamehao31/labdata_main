package com.example.labdata_main.model;

import java.util.List;

public class MixRatioResponse {
    private Long id;
    private String mixName;
    private String mixId;
    private String createdAt;
    private String mixCompany;  // 对应数据库中的mix_company字段
    private List<AsphaltComponentResponse> asphaltComponents;
    private List<SandComponentResponse> sandComponents;
    private List<StoneComponentResponse> stoneComponents;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMixName() {
        return mixName;
    }

    public void setMixName(String mixName) {
        this.mixName = mixName;
    }

    public String getMixId() {
        return mixId;
    }

    public void setMixId(String mixId) {
        this.mixId = mixId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getMixCompany() {
        return mixCompany;
    }

    public void setMixCompany(String mixCompany) {
        this.mixCompany = mixCompany;
    }

    public List<AsphaltComponentResponse> getAsphaltComponents() {
        return asphaltComponents;
    }

    public void setAsphaltComponents(List<AsphaltComponentResponse> asphaltComponents) {
        this.asphaltComponents = asphaltComponents;
    }

    public List<SandComponentResponse> getSandComponents() {
        return sandComponents;
    }

    public void setSandComponents(List<SandComponentResponse> sandComponents) {
        this.sandComponents = sandComponents;
    }

    public List<StoneComponentResponse> getStoneComponents() {
        return stoneComponents;
    }

    public void setStoneComponents(List<StoneComponentResponse> stoneComponents) {
        this.stoneComponents = stoneComponents;
    }

    public static class AsphaltComponentResponse {
        private Long id;
        private Long asphaltId;
        private String asphaltName;
        private Double percentage;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getAsphaltId() {
            return asphaltId;
        }

        public void setAsphaltId(Long asphaltId) {
            this.asphaltId = asphaltId;
        }

        public String getAsphaltName() {
            return asphaltName;
        }

        public void setAsphaltName(String asphaltName) {
            this.asphaltName = asphaltName;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class SandComponentResponse {
        private Long id;
        private Long sandId;
        private String sandName;
        private String gradation;
        private Double percentage;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getSandId() {
            return sandId;
        }

        public void setSandId(Long sandId) {
            this.sandId = sandId;
        }

        public String getSandName() {
            return sandName;
        }

        public void setSandName(String sandName) {
            this.sandName = sandName;
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

    public static class StoneComponentResponse {
        private Long id;
        private Long stoneId;
        private String stoneName;
        private String gradation;
        private Double percentage;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getStoneId() {
            return stoneId;
        }

        public void setStoneId(Long stoneId) {
            this.stoneId = stoneId;
        }

        public String getStoneName() {
            return stoneName;
        }

        public void setStoneName(String stoneName) {
            this.stoneName = stoneName;
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
