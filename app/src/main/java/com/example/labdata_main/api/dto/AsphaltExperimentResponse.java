package com.example.labdata_main.api.dto;

import java.util.List;

public class AsphaltExperimentResponse {
    private Long taskId;
    private String taskName;
    private String description;
    private String status;
    private List<AsphaltData> asphaltDataList;

    public AsphaltExperimentResponse() {
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<AsphaltData> getAsphaltDataList() {
        return asphaltDataList;
    }

    public void setAsphaltDataList(List<AsphaltData> asphaltDataList) {
        this.asphaltDataList = asphaltDataList;
    }

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

        public AsphaltData() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getClientId() {
            return clientId;
        }

        public void setClientId(Long clientId) {
            this.clientId = clientId;
        }

        public String getGrade() {
            return grade;
        }

        public void setGrade(String grade) {
            this.grade = grade;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSupplier() {
            return supplier;
        }

        public void setSupplier(String supplier) {
            this.supplier = supplier;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }

        public List<Long> getExperimentTypeIds() {
            return experimentTypeIds;
        }

        public void setExperimentTypeIds(List<Long> experimentTypeIds) {
            this.experimentTypeIds = experimentTypeIds;
        }

        public List<Long> getAssignmentIds() {
            return assignmentIds;
        }

        public void setAssignmentIds(List<Long> assignmentIds) {
            this.assignmentIds = assignmentIds;
        }

        public List<Long> getAssignmentClientIds() {
            return assignmentClientIds;
        }

        public void setAssignmentClientIds(List<Long> assignmentClientIds) {
            this.assignmentClientIds = assignmentClientIds;
        }
    }
}
