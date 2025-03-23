package com.example.labdata.model;

import com.example.labdata.utils.JsonBinaryType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

// MixtureSplittingTest.java
@Entity
@Table(name = "mixture_splitting_test")
public class MixtureSplittingTest {
    @Id
    @Column(name = "test_id", length = 255)
    private String testId;

    @Column(name = "task_id", length = 255)
    private String taskId;

    @Column(name = "mix_ratio_id", length = 255)
    private String mixRatioId;

    @Column(name = "test_temperature", precision = 6, scale = 2)
    private BigDecimal testTemperature;

    @Column(name = "test_time")
    private Timestamp testTime;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "test_equipment", length = 100)
    private String testEquipment;

    @Column(name = "test_method", length = 100)
    private String testMethod;

    @Column(name = "test_standard", length = 100)
    private String testStandard;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    // getter和setter方法...
}

