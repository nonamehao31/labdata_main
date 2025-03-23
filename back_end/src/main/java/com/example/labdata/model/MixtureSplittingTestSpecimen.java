package com.example.labdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

 // MixtureSplittingTestSpecimen.java
@Entity
@Table(name = "mixture_splitting_test_specimen")
public class MixtureSplittingTestSpecimen {
    @Id
    @Column(name = "specimen_id", length = 255)
    private String specimenId;

    @Column(name = "test_id", length = 255)
    private String testId;

    @Column(name = "specimen_number")
    private Integer specimenNumber;

    @Column(name = "diameter", precision = 6, scale = 2)
    private BigDecimal diameter;

    @Column(name = "height", precision = 6, scale = 2)
    private BigDecimal height;

    @Column(name = "p1_value", precision = 10, scale = 4)
    private BigDecimal p1Value;

    @Column(name = "p2_value", precision = 10, scale = 4)
    private BigDecimal p2Value;

    @Column(name = "p3_value", precision = 10, scale = 4)
    private BigDecimal p3Value;

    @Column(name = "p_average", precision = 10, scale = 4)
    private BigDecimal pAverage;

    @Column(name = "x1_value", precision = 10, scale = 4)
    private BigDecimal x1Value;

    @Column(name = "x2_value", precision = 10, scale = 4)
    private BigDecimal x2Value;

    @Column(name = "x3_value", precision = 10, scale = 4)
    private BigDecimal x3Value;

    @Column(name = "x_average", precision = 10, scale = 4)
    private BigDecimal xAverage;

    @Column(name = "poisson_ratio", precision = 6, scale = 4)
    private BigDecimal poissonRatio;

    @Column(name = "tensile_strength", precision = 10, scale = 4)
    private BigDecimal tensileStrength;

    @Column(name = "failure_strain", precision = 10, scale = 6)
    private BigDecimal failureStrain;

    @Column(name = "stiffness_modulus", precision = 10, scale = 4)
    private BigDecimal stiffnessModulus;

    // getter和setter方法...
}