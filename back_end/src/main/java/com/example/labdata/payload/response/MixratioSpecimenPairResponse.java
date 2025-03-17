package com.example.labdata.payload.response;

/**
 * 配比与制件ID的对应关系响应模型
 * 用于表示任务中配比和制件方法的组合
 */
public class MixratioSpecimenPairResponse {
    private Long mixratioId;
    private Long specimenId;
    private String mixName; // 配比名称
    
    public MixratioSpecimenPairResponse() {
        // 默认构造函数
    }
    
    public MixratioSpecimenPairResponse(Long mixratioId, Long specimenId) {
        this.mixratioId = mixratioId;
        this.specimenId = specimenId;
    }
    
    public MixratioSpecimenPairResponse(Long mixratioId, Long specimenId, String mixName) {
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
        return "MixratioSpecimenPairResponse{" +
                "mixratioId=" + mixratioId +
                ", specimenId=" + specimenId +
                ", mixName='" + mixName + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        MixratioSpecimenPairResponse that = (MixratioSpecimenPairResponse) o;
        
        if (mixratioId != null ? !mixratioId.equals(that.mixratioId) : that.mixratioId != null) return false;
        if (mixName != null ? !mixName.equals(that.mixName) : that.mixName != null) return false;
        return specimenId != null ? specimenId.equals(that.specimenId) : that.specimenId == null;
    }
    
    @Override
    public int hashCode() {
        int result = mixratioId != null ? mixratioId.hashCode() : 0;
        result = 31 * result + (specimenId != null ? specimenId.hashCode() : 0);
        result = 31 * result + (mixName != null ? mixName.hashCode() : 0);
        return result;
    }
}
