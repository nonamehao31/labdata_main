package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 沥青混合料劈裂试验响应数据模型
 */
public class SplittingTestResponse {

    @SerializedName("test_id")
    private String testId;

    @SerializedName("task_id")
    private String taskId;

    @SerializedName("mix_ratio_id")
    private String mixRatioId;

    @SerializedName("test_temperature")
    private Double testTemperature;

    @SerializedName("test_time")
    private String testTime;

    @SerializedName("operator")
    private String operator;

    @SerializedName("test_equipment")
    private String testEquipment;

    @SerializedName("test_method")
    private String testMethod;

    @SerializedName("test_standard")
    private String testStandard;

    @SerializedName("remarks")
    private String remarks;

    @SerializedName("specimens")
    private List<SpecimenData> specimens;

    /**
     * 试件数据模型
     */
    public static class SpecimenData {
        @SerializedName("specimen_id")
        private String specimenId;

        @SerializedName("specimen_number")
        private Integer specimenNumber;

        @SerializedName("diameter")
        private Double diameter;

        @SerializedName("height")
        private Double height;

        @SerializedName("p1_value")
        private Double p1Value;

        @SerializedName("p2_value")
        private Double p2Value;

        @SerializedName("p3_value")
        private Double p3Value;

        @SerializedName("p_average")
        private Double pAverage;

        @SerializedName("x1_value")
        private Double x1Value;

        @SerializedName("x2_value")
        private Double x2Value;

        @SerializedName("x3_value")
        private Double x3Value;

        @SerializedName("x_average")
        private Double xAverage;

        @SerializedName("poisson_ratio")
        private Double poissonRatio;

        @SerializedName("tensile_strength")
        private Double tensileStrength;

        @SerializedName("failure_strain")
        private Double failureStrain;

        @SerializedName("stiffness_modulus")
        private Double stiffnessModulus;

        // Getters and Setters
        public String getSpecimenId() {
            return specimenId;
        }

        public void setSpecimenId(String specimenId) {
            this.specimenId = specimenId;
        }

        public Integer getSpecimenNumber() {
            return specimenNumber;
        }

        public void setSpecimenNumber(Integer specimenNumber) {
            this.specimenNumber = specimenNumber;
        }

        public Double getDiameter() {
            return diameter;
        }

        public void setDiameter(Double diameter) {
            this.diameter = diameter;
        }

        public Double getHeight() {
            return height;
        }

        public void setHeight(Double height) {
            this.height = height;
        }

        public Double getP1Value() {
            return p1Value;
        }

        public void setP1Value(Double p1Value) {
            this.p1Value = p1Value;
        }

        public Double getP2Value() {
            return p2Value;
        }

        public void setP2Value(Double p2Value) {
            this.p2Value = p2Value;
        }

        public Double getP3Value() {
            return p3Value;
        }

        public void setP3Value(Double p3Value) {
            this.p3Value = p3Value;
        }

        public Double getPAverage() {
            return pAverage;
        }

        public void setPAverage(Double pAverage) {
            this.pAverage = pAverage;
        }

        public Double getX1Value() {
            return x1Value;
        }

        public void setX1Value(Double x1Value) {
            this.x1Value = x1Value;
        }

        public Double getX2Value() {
            return x2Value;
        }

        public void setX2Value(Double x2Value) {
            this.x2Value = x2Value;
        }

        public Double getX3Value() {
            return x3Value;
        }

        public void setX3Value(Double x3Value) {
            this.x3Value = x3Value;
        }

        public Double getXAverage() {
            return xAverage;
        }

        public void setXAverage(Double xAverage) {
            this.xAverage = xAverage;
        }

        public Double getPoissonRatio() {
            return poissonRatio;
        }

        public void setPoissonRatio(Double poissonRatio) {
            this.poissonRatio = poissonRatio;
        }

        public Double getTensileStrength() {
            return tensileStrength;
        }

        public void setTensileStrength(Double tensileStrength) {
            this.tensileStrength = tensileStrength;
        }

        public Double getFailureStrain() {
            return failureStrain;
        }

        public void setFailureStrain(Double failureStrain) {
            this.failureStrain = failureStrain;
        }

        public Double getStiffnessModulus() {
            return stiffnessModulus;
        }

        public void setStiffnessModulus(Double stiffnessModulus) {
            this.stiffnessModulus = stiffnessModulus;
        }
    }

    // Getters and Setters
    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(String mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public Double getTestTemperature() {
        return testTemperature;
    }

    public void setTestTemperature(Double testTemperature) {
        this.testTemperature = testTemperature;
    }

    public String getTestTime() {
        return testTime;
    }

    public void setTestTime(String testTime) {
        this.testTime = testTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTestEquipment() {
        return testEquipment;
    }

    public void setTestEquipment(String testEquipment) {
        this.testEquipment = testEquipment;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public String getTestStandard() {
        return testStandard;
    }

    public void setTestStandard(String testStandard) {
        this.testStandard = testStandard;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<SpecimenData> getSpecimens() {
        return specimens;
    }

    public void setSpecimens(List<SpecimenData> specimens) {
        this.specimens = specimens;
    }
}
