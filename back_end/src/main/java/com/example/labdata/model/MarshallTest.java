package com.example.labdata.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 马歇尔试验数据实体类
 */
@Entity
@Table(name = "marshall_test")
public class MarshallTest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "task_id", nullable = false)
    private String taskId;
    
    @Column(name = "stability_1")
    private Float stability1;
    
    @Column(name = "stream_value1")
    private Float streamValue1;
    
    @Column(name = "stability_2")
    private Float stability2;
    
    @Column(name = "stream_value2")
    private Float streamValue2;
    
    @Column(name = "stability_3")
    private Float stability3;
    
    @Column(name = "stream_value3")
    private Float streamValue3;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTaskId() {
        return taskId;
    }
    
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
    
    public Float getStability1() {
        return stability1;
    }
    
    public void setStability1(Float stability1) {
        this.stability1 = stability1;
    }
    
    public Float getStreamValue1() {
        return streamValue1;
    }
    
    public void setStreamValue1(Float streamValue1) {
        this.streamValue1 = streamValue1;
    }
    
    public Float getStability2() {
        return stability2;
    }
    
    public void setStability2(Float stability2) {
        this.stability2 = stability2;
    }
    
    public Float getStreamValue2() {
        return streamValue2;
    }
    
    public void setStreamValue2(Float streamValue2) {
        this.streamValue2 = streamValue2;
    }
    
    public Float getStability3() {
        return stability3;
    }
    
    public void setStability3(Float stability3) {
        this.stability3 = stability3;
    }
    
    public Float getStreamValue3() {
        return streamValue3;
    }
    
    public void setStreamValue3(Float streamValue3) {
        this.streamValue3 = streamValue3;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
