package com.example.labdata.payload.response;

import java.math.BigDecimal;

/**
 * 配比详情响应类
 * 用于返回配比的详细信息，包括材料名称、百分比和级配
 */
public class MixRatioDetailResponse {
    private Long mixratioId;
    private String mixName;
    private String asphaltName;
    private String sandName;
    private String stoneName;
    private BigDecimal asphaltPercentage;
    private BigDecimal sandPercentage;
    private BigDecimal stonePercentage;
    private String sandGradation;
    private String stoneGradation;

    public MixRatioDetailResponse() {
        // 默认构造函数
    }

    public Long getMixratioId() {
        return mixratioId;
    }

    public void setMixratioId(Long mixratioId) {
        this.mixratioId = mixratioId;
    }

    public String getMixName() {
        return mixName;
    }

    public void setMixName(String mixName) {
        this.mixName = mixName;
    }

    public String getAsphaltName() {
        return asphaltName;
    }

    public void setAsphaltName(String asphaltName) {
        this.asphaltName = asphaltName;
    }

    public String getSandName() {
        return sandName;
    }

    public void setSandName(String sandName) {
        this.sandName = sandName;
    }

    public String getStoneName() {
        return stoneName;
    }

    public void setStoneName(String stoneName) {
        this.stoneName = stoneName;
    }

    public BigDecimal getAsphaltPercentage() {
        return asphaltPercentage;
    }

    public void setAsphaltPercentage(BigDecimal asphaltPercentage) {
        this.asphaltPercentage = asphaltPercentage;
    }

    public BigDecimal getSandPercentage() {
        return sandPercentage;
    }

    public void setSandPercentage(BigDecimal sandPercentage) {
        this.sandPercentage = sandPercentage;
    }

    public BigDecimal getStonePercentage() {
        return stonePercentage;
    }

    public void setStonePercentage(BigDecimal stonePercentage) {
        this.stonePercentage = stonePercentage;
    }

    public String getSandGradation() {
        return sandGradation;
    }

    public void setSandGradation(String sandGradation) {
        this.sandGradation = sandGradation;
    }

    public String getStoneGradation() {
        return stoneGradation;
    }

    public void setStoneGradation(String stoneGradation) {
        this.stoneGradation = stoneGradation;
    }

    @Override
    public String toString() {
        return "MixRatioDetailResponse{" +
                "mixratioId=" + mixratioId +
                ", mixName='" + mixName + '\'' +
                ", asphaltName='" + asphaltName + '\'' +
                ", sandName='" + sandName + '\'' +
                ", stoneName='" + stoneName + '\'' +
                ", asphaltPercentage=" + asphaltPercentage +
                ", sandPercentage=" + sandPercentage +
                ", stonePercentage=" + stonePercentage +
                ", sandGradation='" + sandGradation + '\'' +
                ", stoneGradation='" + stoneGradation + '\'' +
                '}';
    }
}
