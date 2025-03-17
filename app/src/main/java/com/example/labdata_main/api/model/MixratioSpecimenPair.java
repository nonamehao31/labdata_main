package com.example.labdata_main.api.model;

/**
 * 配比与制件ID的对应关系模型
 * 用于表示任务中配比和制件方法的组合
 */
public class MixratioSpecimenPair {
    private Long mixratioId;
    private Long specimenId;
    private String mixName; // 配比名称
    
    public MixratioSpecimenPair() {
        // 默认构造函数，用于Gson反序列化
    }
    
    public MixratioSpecimenPair(Long mixratioId, Long specimenId) {
        this.mixratioId = mixratioId;
        this.specimenId = specimenId;
    }
    
    public MixratioSpecimenPair(Long mixratioId, Long specimenId, String mixName) {
        this.mixratioId = mixratioId;
        this.specimenId = specimenId;
        this.mixName = mixName;
    }
    
    public Long getMixratioId() {
        return mixratioId;
    }
    
    public void setMixratioId(Long mixratioId) {
        this.mixratioId = mixratioId;
    }
    
    public Long getSpecimenId() {
        return specimenId;
    }
    
    public void setSpecimenId(Long specimenId) {
        this.specimenId = specimenId;
    }
    
    public String getMixName() {
        return mixName;
    }
    
    public void setMixName(String mixName) {
        this.mixName = mixName;
    }
    
    @Override
    public String toString() {
        return "MixratioSpecimenPair{" +
                "mixratioId=" + mixratioId +
                ", specimenId=" + specimenId +
                ", mixName='" + mixName + '\'' +
                '}';
    }
}
