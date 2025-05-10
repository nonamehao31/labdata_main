package com.example.labdata_main.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExperimentAssignment {
    public static final String EXPERIMENT_COMPRESSION = "compression";
    public static final String EXPERIMENT_FLEXURAL = "flexural";
    public static final String EXPERIMENT_SPLITTING = "splitting";
    public static final String EXPERIMENT_ELASTIC = "elastic";
    public static final String EXPERIMENT_MARSHALL = "marshall";
    public static final String EXPERIMENT_BEAM = "beam";

    private List<String> experimentTypes;
    private String notes;

    public ExperimentAssignment() {
        experimentTypes = new ArrayList<>();
    }

    public List<String> getExperimentTypes() {
        return experimentTypes;
    }

    public void setExperimentTypes(List<String> experimentTypes) {
        this.experimentTypes = experimentTypes;
    }

    public void addExperimentType(String type) {
        if (!experimentTypes.contains(type)) {
            experimentTypes.add(type);
        }
    }

    public void removeExperimentType(String type) {
        experimentTypes.remove(type);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // 添加实验类型
        List<String> experimentNames = new ArrayList<>();
        for (String type : experimentTypes) {
            switch (type) {
                case EXPERIMENT_COMPRESSION:
                    experimentNames.add("抗压强度");
                    break;
                case EXPERIMENT_FLEXURAL:
                    experimentNames.add("抗折强度");
                    break;
                case EXPERIMENT_SPLITTING:
                    experimentNames.add("劈裂强度");
                    break;
                case EXPERIMENT_ELASTIC:
                    experimentNames.add("弹性模量");
                    break;
                case EXPERIMENT_MARSHALL:
                    experimentNames.add("马歇尔试验");
                    break;
                case EXPERIMENT_BEAM:
                    experimentNames.add("梁试验");
                    break;
            }
        }
        sb.append(String.join("、", experimentNames));
        
        return sb.toString();
    }
}
