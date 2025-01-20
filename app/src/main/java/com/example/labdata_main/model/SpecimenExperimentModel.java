package com.example.labdata_main.model;

import java.util.ArrayList;
import java.util.List;

public class SpecimenExperimentModel {
    private int specimenNumber;
    private String specimenShape;
    private List<String> selectedExperiments;

    public SpecimenExperimentModel(int specimenNumber, String specimenShape) {
        this.specimenNumber = specimenNumber;
        this.specimenShape = specimenShape;
        this.selectedExperiments = new ArrayList<>();
    }

    public int getSpecimenNumber() {
        return specimenNumber;
    }

    public String getSpecimenShape() {
        return specimenShape;
    }

    public List<String> getSelectedExperiments() {
        return selectedExperiments;
    }

    public void addExperiment(String experiment) {
        if (!selectedExperiments.contains(experiment)) {
            selectedExperiments.add(experiment);
        }
    }

    public void removeExperiment(String experiment) {
        selectedExperiments.remove(experiment);
    }

    public boolean hasAllExperimentsAssigned() {
        // 检查是否包含了全部三种实验
        return selectedExperiments.contains("马歇尔稳定度") && 
               selectedExperiments.contains("弯曲梁") && 
               selectedExperiments.contains("弹性模量");
    }
}
