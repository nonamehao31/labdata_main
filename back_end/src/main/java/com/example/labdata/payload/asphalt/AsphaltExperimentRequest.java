package com.example.labdata.payload.asphalt;

import lombok.Data;
import java.util.List;

@Data
public class AsphaltExperimentRequest {
    private Long id;
    private String taskName;
    private String description;
    private Long clientId;
    private List<AsphaltData> asphaltDataList;

    @Data
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
