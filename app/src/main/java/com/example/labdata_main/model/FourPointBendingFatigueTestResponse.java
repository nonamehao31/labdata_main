package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 沥青混合料四点弯曲疲劳寿命实验响应模型
 */
public class FourPointBendingFatigueTestResponse {
    @SerializedName("task_id")
    private String taskId;

    @SerializedName("test_date")
    private String testDate;

    @SerializedName("operator")
    private String operator;

    @SerializedName("specimens")
    private List<SpecimenData> specimens;

    /**
 * 试件数据
 */
public static class SpecimenData {
    @SerializedName("specimen_number")
    private int specimenNumber;

    @SerializedName("length_mm")
    private double length;

    @SerializedName("width_mm")
    private double width;

    @SerializedName("height_mm")
    private double height;

    @SerializedName("span_mm")
    private double spanMm;

    @SerializedName("strain_range")
    private double strainRange;

    @SerializedName("frequency_hz")
    private double frequencyHz;

    @SerializedName("test_temperature")
    private double testTemperature;

    @SerializedName("fatigue_life")
    private double fatigueLife;

    @SerializedName("results")
    private List<ResultData> results;

    /**
     * 实验结果数据
     */
    public static class ResultData {
        @SerializedName("result_type")
        private String resultType;

        @SerializedName("result_type_display_name")
        private String resultTypeDisplayName;

        @SerializedName("result_type_english_name")
        private String resultTypeEnglishName;

        @SerializedName("result_type_unit")
        private String resultTypeUnit;

        @SerializedName("initial_value")
        private double initialValue;

        @SerializedName("current_value")
        private double currentValue;

        @SerializedName("result_index")
        private Integer resultIndex;

        // Getters and Setters
        public String getResultType() {
            return resultType;
        }

        public void setResultType(String resultType) {
            this.resultType = resultType;
        }

        public String getResultTypeDisplayName() {
            return resultTypeDisplayName;
        }

        public void setResultTypeDisplayName(String resultTypeDisplayName) {
            this.resultTypeDisplayName = resultTypeDisplayName;
        }

        public String getResultTypeEnglishName() {
            return resultTypeEnglishName;
        }

        public void setResultTypeEnglishName(String resultTypeEnglishName) {
            this.resultTypeEnglishName = resultTypeEnglishName;
        }

        public String getResultTypeUnit() {
            return resultTypeUnit;
        }

        public void setResultTypeUnit(String resultTypeUnit) {
            this.resultTypeUnit = resultTypeUnit;
        }

        public double getInitialValue() {
            return initialValue;
        }

        public void setInitialValue(double initialValue) {
            this.initialValue = initialValue;
        }

        public double getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(double currentValue) {
            this.currentValue = currentValue;
        }

        public Integer getResultIndex() {
            return resultIndex;
        }

        public void setResultIndex(Integer resultIndex) {
            this.resultIndex = resultIndex;
        }
    }

    // Getters and Setters
    public int getSpecimenNumber() {
        return specimenNumber;
    }

    public void setSpecimenNumber(int specimenNumber) {
        this.specimenNumber = specimenNumber;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getSpanMm() {
        return spanMm;
    }

    public void setSpanMm(double spanMm) {
        this.spanMm = spanMm;
    }

    public double getStrainRange() {
        return strainRange;
    }

    public void setStrainRange(double strainRange) {
        this.strainRange = strainRange;
    }

    public double getFrequencyHz() {
        return frequencyHz;
    }

    public void setFrequencyHz(double frequencyHz) {
        this.frequencyHz = frequencyHz;
    }

    public double getTestTemperature() {
        return testTemperature;
    }

    public void setTestTemperature(double testTemperature) {
        this.testTemperature = testTemperature;
    }

    public double getFatigueLife() {
        return fatigueLife;
    }

    public void setFatigueLife(double fatigueLife) {
        this.fatigueLife = fatigueLife;
    }

    public List<ResultData> getResults() {
        return results;
    }

    public void setResults(List<ResultData> results) {
        this.results = results;
    }
}

// Getters and Setters
public String getTaskId() {
    return taskId;
}

public void setTaskId(String taskId) {
    this.taskId = taskId;
}

public String getTestDate() {
    return testDate;
}

public void setTestDate(String testDate) {
    this.testDate = testDate;
}

public String getOperator() {
    return operator;
}

public void setOperator(String operator) {
    this.operator = operator;
}

public List<SpecimenData> getSpecimens() {
    return specimens;
}

public void setSpecimens(List<SpecimenData> specimens) {
    this.specimens = specimens;
}
}
