package com.example.labdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mixratio_asphalt")
public class MixRatioAsphalt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mixratio_id", nullable = false)
    private MixRatio mixRatio;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asphalt_id", nullable = false)
    private AsphaltMaterial asphaltMaterial;

    @NotNull(message = "沥青百分比不能为空")
    @DecimalMin(value = "0.0", message = "沥青百分比不能小于0")
    @DecimalMax(value = "100.0", message = "沥青百分比不能大于100")
    @Column(name = "percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentage;
}
