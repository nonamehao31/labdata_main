package com.example.labdata_main.model;

import java.util.HashSet;
import java.util.Set;

public class MixExperiment {
    private String mixTitle;
    private Set<String> selectedExperiments;

    public MixExperiment(String mixTitle) {
        this.mixTitle = mixTitle;
        this.selectedExperiments = new HashSet<>();
    }

    public String getMixTitle() {
        return mixTitle;
    }

    public void setMixTitle(String mixTitle) {
        this.mixTitle = mixTitle;
    }

    public Set<String> getSelectedExperiments() {
        return selectedExperiments;
    }

    public void addExperiment(String experiment) {
        selectedExperiments.add(experiment);
    }

    public void removeExperiment(String experiment) {
        selectedExperiments.remove(experiment);
    }

    public boolean hasExperiment(String experiment) {
        return selectedExperiments.contains(experiment);
    }
}
