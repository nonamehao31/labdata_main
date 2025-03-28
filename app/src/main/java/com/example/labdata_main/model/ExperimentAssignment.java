package com.example.labdata_main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentAssignment {
    public static final String EXPERIMENT_COMPRESSION = "compression";
    public static final String EXPERIMENT_FLEXURAL = "flexural";
    public static final String EXPERIMENT_SPLITTING = "splitting";
    public static final String EXPERIMENT_ELASTIC = "elastic";

    private List<String> experimentTypes;
    private int curingAge;
    private Map<Long, List<String>> mixRatioExperiments;
    private String notes;
    private String assignmentName;  // 实验指派名称
    private String assignmentDetails;  // 实验指派详情
    private boolean finished;  // 任务是否已完成

    public ExperimentAssignment() {
        experimentTypes = new ArrayList<>();
        mixRatioExperiments = new HashMap<>();
        finished = false;
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

    public int getCuringAge() {
        return curingAge;
    }

    public void setCuringAge(int curingAge) {
        this.curingAge = curingAge;
    }

    public Map<Long, List<String>> getMixRatioExperiments() {
        return mixRatioExperiments;
    }

    public void addMixRatioExperiments(long mixRatioId, List<String> experimentTypes) {
        mixRatioExperiments.put(mixRatioId, new ArrayList<>(experimentTypes));
    }

    public List<String> getExperimentsForMixRatio(long mixRatioId) {
        return mixRatioExperiments.get(mixRatioId);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getAssignmentDetails() {
        return assignmentDetails;
    }

    public void setAssignmentDetails(String assignmentDetails) {
        this.assignmentDetails = assignmentDetails;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
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
                    experimentNames.add("劈裂抗拉");
                    break;
                case EXPERIMENT_ELASTIC:
                    experimentNames.add("弹性模量");
                    break;
            }
        }
        sb.append(String.join("、", experimentNames));
        
        // 添加养护龄期
        sb.append(String.format(" %dd", curingAge));
        
        // 添加每个配比的实验类型
        for (Map.Entry<Long, List<String>> entry : mixRatioExperiments.entrySet()) {
            sb.append("\n配比").append(entry.getKey()).append(": ");
            
            List<String> mixRatioExperimentNames = new ArrayList<>();
            for (String type : entry.getValue()) {
                switch (type) {
                    case EXPERIMENT_COMPRESSION:
                        mixRatioExperimentNames.add("抗压强度");
                        break;
                    case EXPERIMENT_FLEXURAL:
                        mixRatioExperimentNames.add("抗折强度");
                        break;
                    case EXPERIMENT_SPLITTING:
                        mixRatioExperimentNames.add("劈裂抗拉");
                        break;
                    case EXPERIMENT_ELASTIC:
                        mixRatioExperimentNames.add("弹性模量");
                        break;
                }
            }
            sb.append(String.join("、", mixRatioExperimentNames));
        }
        
        // 添加备注
        if (notes != null && !notes.isEmpty()) {
            sb.append("\n备注: ").append(notes);
        }
        
        // 添加实验指派名称和详情
        if (assignmentName != null && !assignmentName.isEmpty()) {
            sb.append("\n实验指派名称: ").append(assignmentName);
        }
        if (assignmentDetails != null && !assignmentDetails.isEmpty()) {
            sb.append("\n实验指派详情: ").append(assignmentDetails);
        }
        
        // 添加任务完成状态
        sb.append("\n任务完成状态: ").append(finished ? "已完成" : "未完成");
        
        return sb.toString();
    }
}
