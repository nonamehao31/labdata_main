package com.example.labdata.payload.asphalt;

import com.example.labdata.model.ExperimentTask;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AsphaltExperimentResponse {
    private Long taskId;
    private String taskName;
    private String description;
    private String status;
    private List<AsphaltData> asphaltDataList;

    public AsphaltExperimentResponse(ExperimentTask task, List<AsphaltData> asphaltDataList) {
        this.taskId = task.getId();
        this.taskName = task.getName();
        this.description = task.getDescription();
        this.status = task.getStatus().toString();
        this.asphaltDataList = asphaltDataList;
    }

    @Data
    @NoArgsConstructor
    public static class AsphaltData {
        private Long id;
        private Long clientId;
        private String grade;
        private String type;
        private String supplier;
        private String expiryDate;
        private List<Long> experimentTypeIds;
        private List<Long> assignmentIds;
        private List<Long> assignmentClientIds;
    }
}
