package com.example.labdata.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * 沥青实验任务实体类
 */
@Entity
@Table(name = "asphalt_task")
public class AsphaltTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asphalt_experiment_id")
    private Long asphaltExperimentId;

    @Column(name = "asphalt_experiment_name", nullable = false)
    private String asphaltExperimentName;

    @Column(name = "asphalt_experiment_type", nullable = false)
    private String asphaltExperimentType;
    
    @Column(name = "asphalt_task_name", nullable = false)
    private String asphaltTaskName;
    
    @Column(name = "asphalt_task_assignment", nullable = false)
    private String asphaltTaskAssignment;
    
    @Column(name = "asphalt_task_assignment_id")
    private String asphaltTaskAssignmentId;
    
    @Column(name = "task_status", nullable = false)
    private String taskStatus;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "selected_asphalt_id")
    private Long selectedAsphaltId;
    
    @Column(name = "company_id")
    private String companyId;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public AsphaltTask() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Long getAsphaltExperimentId() {
        return asphaltExperimentId;
    }

    public void setAsphaltExperimentId(Long asphaltExperimentId) {
        this.asphaltExperimentId = asphaltExperimentId;
    }

    public String getAsphaltExperimentName() {
        return asphaltExperimentName;
    }

    public void setAsphaltExperimentName(String asphaltExperimentName) {
        this.asphaltExperimentName = asphaltExperimentName;
    }

    public String getAsphaltExperimentType() {
        return asphaltExperimentType;
    }

    public void setAsphaltExperimentType(String asphaltExperimentType) {
        this.asphaltExperimentType = asphaltExperimentType;
    }

    public String getAsphaltTaskName() {
        return asphaltTaskName;
    }

    public void setAsphaltTaskName(String asphaltTaskName) {
        this.asphaltTaskName = asphaltTaskName;
    }

    public String getAsphaltTaskAssignment() {
        return asphaltTaskAssignment;
    }

    public void setAsphaltTaskAssignment(String asphaltTaskAssignment) {
        this.asphaltTaskAssignment = asphaltTaskAssignment;
    }

    public String getAsphaltTaskAssignmentId() {
        return asphaltTaskAssignmentId;
    }

    public void setAsphaltTaskAssignmentId(String asphaltTaskAssignmentId) {
        this.asphaltTaskAssignmentId = asphaltTaskAssignmentId;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSelectedAsphaltId() {
        return selectedAsphaltId;
    }

    public void setSelectedAsphaltId(Long selectedAsphaltId) {
        this.selectedAsphaltId = selectedAsphaltId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
