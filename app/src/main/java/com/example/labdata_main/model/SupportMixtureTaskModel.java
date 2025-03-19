package com.example.labdata_main.model;

import com.google.gson.annotations.SerializedName;

/**
 * 支持的混合料任务类型模型
 */
public class SupportMixtureTaskModel {
    @SerializedName("taskId")
    private int id;
    
    @SerializedName("taskName")
    private String name;
    
    @SerializedName("taskType")
    private String type;
    
    // 默认构造函数
    public SupportMixtureTaskModel() {
    }
    
    // 带参数的构造函数
    public SupportMixtureTaskModel(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return "SupportMixtureTaskModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
