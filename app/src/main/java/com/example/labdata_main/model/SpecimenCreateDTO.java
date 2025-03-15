package com.example.labdata_main.model;

/**
 * 用于创建Specimen的数据传输对象，不包含ID字段
 * 这是为了解决创建新Specimen时ID默认值(0)被发送到后端的问题
 */
public class SpecimenCreateDTO {
    private Long mixRatioId;
    private float mixingTemperature;
    private float mixingSpeed;
    private Integer mixingTime;
    private String compactionMethod;
    private long creationTime;
    private String cutShape;
    private int cutCount;
    private float length;
    private float width;
    private float height;
    private float radius;
    private Long createdBy; // 添加创建者ID字段
    private Long specimenCompany; // 添加所属单位ID字段
    
    // 从Specimen对象创建DTO，但不包含ID
    public static SpecimenCreateDTO fromSpecimen(Specimen specimen) {
        SpecimenCreateDTO dto = new SpecimenCreateDTO();
        dto.mixRatioId = specimen.getMixRatioId();
        dto.mixingTemperature = specimen.getMixingTemperature();
        dto.mixingSpeed = specimen.getMixingSpeed();
        dto.mixingTime = specimen.getMixingTime();
        dto.compactionMethod = specimen.getCompactionMethod();
        dto.creationTime = specimen.getCreationTime();
        dto.cutShape = specimen.getCutShape();
        dto.cutCount = specimen.getCutCount();
        dto.length = specimen.getLength();
        dto.width = specimen.getWidth();
        dto.height = specimen.getHeight();
        dto.radius = specimen.getRadius();
        dto.createdBy = specimen.getCreatedBy(); // 添加创建者ID
        dto.specimenCompany = specimen.getSpecimenCompany(); // 添加所属单位ID
        return dto;
    }

    public Long getMixRatioId() {
        return mixRatioId;
    }

    public void setMixRatioId(Long mixRatioId) {
        this.mixRatioId = mixRatioId;
    }

    public float getMixingTemperature() {
        return mixingTemperature;
    }

    public void setMixingTemperature(float mixingTemperature) {
        this.mixingTemperature = mixingTemperature;
    }

    public float getMixingSpeed() {
        return mixingSpeed;
    }

    public void setMixingSpeed(float mixingSpeed) {
        this.mixingSpeed = mixingSpeed;
    }

    public Integer getMixingTime() {
        return mixingTime;
    }

    public void setMixingTime(Integer mixingTime) {
        this.mixingTime = mixingTime;
    }

    public String getCompactionMethod() {
        return compactionMethod;
    }

    public void setCompactionMethod(String compactionMethod) {
        this.compactionMethod = compactionMethod;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getCutShape() {
        return cutShape;
    }

    public void setCutShape(String cutShape) {
        this.cutShape = cutShape;
    }

    public int getCutCount() {
        return cutCount;
    }

    public void setCutCount(int cutCount) {
        this.cutCount = cutCount;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getSpecimenCompany() {
        return specimenCompany;
    }
    
    public void setSpecimenCompany(Long specimenCompany) {
        this.specimenCompany = specimenCompany;
    }
}
