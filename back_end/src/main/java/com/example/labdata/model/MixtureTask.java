package com.example.labdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;

@Entity
@Table(name = "mixture_task")
public class MixtureTask {
    @Id
    @Column(name = "task_id")
    private String taskId;
    
    @Column(name = "task_name")
    private String taskName;
    
    @Column(name = "task_type")
    private String taskType;
    
    @Column(name = "project_id", columnDefinition = "varchar(255)")
    private String projectId;
    
    @Column(name = "prepare_status")
    private String prepareStatus;
    
    @Column(name = "making_status")
    private String makingStatus;
    
    @Column(name = "testing_status")
    private String testingStatus;
    
    @Column(name = "assigned_mixing_equipment")
    private String assignedMixingEquipment;
    
    @Column(name = "assigned_forming_equipment")
    private String assignedFormingEquipment;
    
    @Column(name = "assigned_testing_equipment")
    private String assignedTestingEquipment;
    
    @Column(name = "mixing_equipment_manufacturer")
    private String mixingEquipmentManufacturer;
    
    @Column(name = "forming_equipment_manufacturer")
    private String formingEquipmentManufacturer;
    
    @Column(name = "testing_equipment_manufacturer")
    private String testingEquipmentManufacturer;

    
    // Getter和Setter

    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;
    
    @Transient
    private String projectName;
    
    // Constructors
    public MixtureTask() {
        this.prepareStatus = "unfinished";
        this.makingStatus = "unfinished";
        this.testingStatus = "unfinished";
    }

    public MixtureTask(String taskId, String taskName, String taskType) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskType = taskType;
        this.prepareStatus = "unfinished";
        this.makingStatus = "unfinished";
        this.testingStatus = "unfinished";
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.prepareStatus == null) this.prepareStatus = "unfinished";
        if (this.makingStatus == null) this.makingStatus = "unfinished";
        if (this.testingStatus == null) this.testingStatus = "unfinished";
    }
    
    // Getters and Setters
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
    
    public String getTaskType() {
        return taskType;
    }
    
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }
    
    public String getProjectId() {
        return projectId;
    }
    
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
    
    public String getPrepareStatus() {
        return prepareStatus;
    }
    
    public void setPrepareStatus(String prepareStatus) {
        this.prepareStatus = prepareStatus;
    }
    
    public String getMakingStatus() {
        return makingStatus;
    }
    
    public void setMakingStatus(String makingStatus) {
        this.makingStatus = makingStatus;
    }
    
    public String getTestingStatus() {
        return testingStatus;
    }
    
    public void setTestingStatus(String testingStatus) {
        this.testingStatus = testingStatus;
    }
    
    public String getAssignedMixingEquipment() {
        return assignedMixingEquipment;
    }
    
    public void setAssignedMixingEquipment(String assignedMixingEquipment) {
        this.assignedMixingEquipment = assignedMixingEquipment;
    }
    
    public String getAssignedFormingEquipment() {
        return assignedFormingEquipment;
    }
    
    public void setAssignedFormingEquipment(String assignedFormingEquipment) {
        this.assignedFormingEquipment = assignedFormingEquipment;
    }
    
    public String getAssignedTestingEquipment() {
        return assignedTestingEquipment;
    }
    
    public void setAssignedTestingEquipment(String assignedTestingEquipment) {
        this.assignedTestingEquipment = assignedTestingEquipment;
    }
    
    public String getMixingEquipmentManufacturer() {
        return mixingEquipmentManufacturer;
    }
    
    public void setMixingEquipmentManufacturer(String mixingEquipmentManufacturer) {
        this.mixingEquipmentManufacturer = mixingEquipmentManufacturer;
    }
    
    public String getFormingEquipmentManufacturer() {
        return formingEquipmentManufacturer;
    }
    
    public void setFormingEquipmentManufacturer(String formingEquipmentManufacturer) {
        this.formingEquipmentManufacturer = formingEquipmentManufacturer;
    }
    
    public String getTestingEquipmentManufacturer() {
        return testingEquipmentManufacturer;
    }
    
    public void setTestingEquipmentManufacturer(String testingEquipmentManufacturer) {
        this.testingEquipmentManufacturer = testingEquipmentManufacturer;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getProjectName() {
        // 优先使用从数据库获取的项目名称
        if (project != null && project.getName() != null) {
            return project.getName();
        }
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
