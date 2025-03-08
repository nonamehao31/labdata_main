package com.example.labdata.model;

import com.example.labdata.model.audit.DateAudit;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 支持设备信息实体类
 * 用于存储系统支持的所有设备类型、厂商和型号信息
 */
@Entity
@Table(name = "supported_devices")
public class SupportedDevice extends DateAudit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    private String type; // 设备类型: MIXING, FORMING, TESTING, SIEVING
    
    @NotBlank
    @Size(max = 100)
    private String manufacturer; // 厂商名称
    
    @NotBlank
    @Size(max = 100)
    private String model; // 设备型号
    
    @Size(max = 255)
    private String description; // 设备描述（可选）
    
    public SupportedDevice() {
    }
    
    public SupportedDevice(String type, String manufacturer, String model) {
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
    }
    
    public SupportedDevice(String type, String manufacturer, String model, String description) {
        this.type = type;
        this.manufacturer = manufacturer;
        this.model = model;
        this.description = description;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getManufacturer() {
        return manufacturer;
    }
    
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
